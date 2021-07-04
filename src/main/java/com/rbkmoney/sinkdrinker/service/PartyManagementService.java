package com.rbkmoney.sinkdrinker.service;

import com.rbkmoney.damsel.domain.Party;
import com.rbkmoney.damsel.domain.Shop;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.sinkdrinker.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartyManagementService {

    private final UserInfo userInfo = new UserInfo("admin", UserType.internal_user(new InternalUser()));

    private final PartyManagementSrv.Iface partyManagementClient;

    public String getPayoutToolId(String partyId, String shopId) {
        Party party = getParty(partyId);
        return Optional.ofNullable(party.getShops().get(shopId))
                .map(Shop::getPayoutToolId)
                .orElse("-1");
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
