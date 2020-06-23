package edu.kit.scc.webreg.service.twofa;

public interface TwoFaService {

	void findByUserId(Long userId) throws TwoFaException;

}
