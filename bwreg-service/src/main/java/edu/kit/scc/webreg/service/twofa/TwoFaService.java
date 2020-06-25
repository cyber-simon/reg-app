package edu.kit.scc.webreg.service.twofa;

import java.util.List;

public interface TwoFaService {

	List<?> findByUserId(Long userId) throws TwoFaException;

}
