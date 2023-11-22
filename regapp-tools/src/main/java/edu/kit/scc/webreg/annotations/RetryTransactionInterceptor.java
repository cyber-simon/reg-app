package edu.kit.scc.webreg.annotations;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Status;
import jakarta.transaction.TransactionalException;
import jakarta.transaction.UserTransaction;

@Interceptor
@RetryTransaction
public class RetryTransactionInterceptor {

	public static final Logger logger = LoggerFactory.getLogger(RetryTransactionInterceptor.class);

	@AroundInvoke
	public Object retry(InvocationContext invocationCtx) throws Exception {
		UserTransaction userTransaction = CDI.current().select(UserTransaction.class).get();

		Method method = invocationCtx.getMethod();
		RetryTransaction annotation = method.getAnnotation(RetryTransaction.class);
		int maxRetries = annotation.retries();

		int retries = 0;
		long startTime = 0L;
		long endTime = 0L;
		boolean traceEnabled = logger.isTraceEnabled() || annotation.trace();

		while (retries < maxRetries) {
			try {
				if (retries > 0) {
					logger.info("Trying to call method {} [{}] retry: {} (out of {})", method.getName(),
							Thread.currentThread().getName(), retries, maxRetries);
				}

				if (traceEnabled) {
					logger.info("Entering timing of method {} [{}], retry {} (out of {})", method.getName(),
							Thread.currentThread().getName(), retries, maxRetries);
					startTime = System.currentTimeMillis();
				}

				userTransaction.begin();
				Object returnValue = invocationCtx.proceed();
				userTransaction.commit();

				if (traceEnabled) {
					endTime = System.currentTimeMillis();
					logger.info("method timing {} [{}]: {} ms", method.getName(), Thread.currentThread().getName(),
							(endTime - startTime));
				}

				return returnValue;
			} catch (Exception e) {

				Throwable cause = e.getCause();
				if (!(e instanceof OptimisticLockException || cause != null
						|| cause instanceof OptimisticLockException)) {
					logger.debug("Other Exception {}", e.getMessage());
					throw e;
				} else {
					logger.debug("Exception or cause is Opt lock. Retrying. Message: {}", e.getMessage());
				}
			} finally {
				/*
				 * Clean up
				 */
				try {
					if (traceEnabled) {
						logger.info("method {} clean up, transaction is status {}", method.getName(),
								userTransaction.getStatus());
					}

					if (userTransaction.getStatus() == Status.STATUS_ACTIVE)
						userTransaction.rollback();
					else if (userTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK)
						userTransaction.rollback();

				} catch (Throwable e) {
					if (traceEnabled) {
						logger.info("method {} ignored exception: {}", e.getMessage());
					}
				}
			}
			retries++;
		}
		
		throw new TransactionalException("tranaction in method " + method.getName() + " failed with 10 retries", null);
	}
}
