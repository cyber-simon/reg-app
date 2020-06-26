package edu.kit.scc.webreg.service.twofa;

public interface TwoFaService {

	LinotpTokenResultList findByUserId(Long userId) throws TwoFaException;

}
