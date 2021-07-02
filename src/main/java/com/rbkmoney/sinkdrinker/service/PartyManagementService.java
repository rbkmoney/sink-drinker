package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.damsel.domain.Party;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.sinkdrinker.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartyManagementService {

    private final UserInfo userInfo = new UserInfo("admin", UserType.internal_user(new InternalUser()));

    private final PartyManagementSrv.Iface partyManagementClient;

    public String getPayoutToolId(String partyId, String shopId) {
        Party party = getParty(partyId);
        String payoutToolId = party.getShops().get(shopId).getPayoutToolId();
        if (payoutToolId == null) {
            throw new NotFoundException(
                    String.format("PayoutToolId is null with partyId=%s, shopId=%s", partyId, shopId));
        }
        return payoutToolId;
    }

    private Party getParty(String partyId) throws NotFoundException {
        log.debug("Trying to get party, partyId='{}'", partyId);
        try {
            Party party = partyManagementClient.get(userInfo, partyId);
            log.debug("Party has been found, partyId='{}'", partyId);
            return party;
        } catch (PartyNotFound ex) {
            throw new NotFoundException(
                    String.format("Party not found, partyId='%s'", partyId), ex);
        } catch (InvalidPartyRevision ex) {
            throw new NotFoundException(
                    String.format("Invalid party revision, partyId='%s'", partyId), ex);
        } catch (TException ex) {
            throw new RuntimeException(
                    String.format("Failed to get party, partyId='%s'", partyId), ex);
        }
    }
}
