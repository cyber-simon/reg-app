package edu.kit.scc.webreg.annotations;

import javax.enterprise.inject.spi.CDI;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.OptimisticLockException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.exc.MisconfiguredServiceException;

@Interceptor
@RetryTransaction
public class RetryTransactionInterceptor {

	public static final Logger logger = LoggerFactory.getLogger(RetryTransactionInterceptor.class);

	@AroundInvoke
	public Object retry(InvocationContext invocationCtx) throws Exception {
		UserTransaction userTransaction = CDI.current().select(UserTransaction.class).get();

		int retries = 0;
		while (retries < 10) {
			try {
				if (logger.isTraceEnabled()) {
					logger.trace("Entering timing of method {} [{}], retry {}", invocationCtx.getMethod().getName(),
							Thread.currentThread().getName(), retries);
				}

				if (retries > 0) {
					logger.info("Trying to call method {} [{}] retry: ", invocationCtx.getMethod().getName(),
							Thread.currentThread().getName(), (retries + 1));
				}

				long startTime = System.currentTimeMillis();
				userTransaction.begin();
				Object returnValue = invocationCtx.proceed();
				userTransaction.commit();
				long endTime = System.currentTimeMillis();
				if (logger.isTraceEnabled()) {
					logger.trace("method timing {} [{}]: {} ms", invocationCtx.getMethod().getName(),
							Thread.currentThread().getName(), (endTime - startTime));
				}
				return returnValue;
			} catch (Exception e) {

				Throwable cause = e.getCause();
				if (!(e instanceof OptimisticLockException || cause != null
						|| cause instanceof OptimisticLockException)) {
					logger.trace("Other Exception {}", e.getMessage());
					throw e;
				} else {
					logger.trace("Exception or cause is Opt lock. Retrying. Message: {}", e.getMessage());
				}
			} finally {
				/*
				 * Clean up
				 */
				try {
					if (logger.isTraceEnabled()) {
						logger.trace("method {} clean up, transaction is status {}",
								invocationCtx.getMethod().getName(), userTransaction.getStatus());
					}

					if (userTransaction.getStatus() == Status.STATUS_ACTIVE)
						userTransaction.rollback();
					else if (userTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK)
						userTransaction.rollback();

				} catch (Throwable e) {
					if (logger.isTraceEnabled()) {
						logger.trace("method {} ignored exception: {}", e.getMessage());
					}
				}
			}
			retries++;
		}
		throw new MisconfiguredServiceException("tranaction failed with 10 retries");

	}
}
