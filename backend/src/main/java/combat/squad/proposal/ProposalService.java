package combat.squad.proposal;

import combat.squad.event.EventEntity;
import combat.squad.event.EventRepository;
import combat.squad.event.EventService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProposalService {
    private final ProposalRepository proposalRepository;
    private final EventRepository eventRepository;

    public ProposalService(ProposalRepository proposalRepository, EventRepository eventRepository) {
        this.proposalRepository = proposalRepository;
        this.eventRepository = eventRepository;
    }

    public List<ProposalEntity> getProposals() {
        return this.proposalRepository.findAll();
    }

    public ProposalEntity getProposalById(UUID id) {
        return this.proposalRepository.findById(id).orElseThrow();
    }

    public ProposalEntity createProposal(ProposalDto proposalDTO, UUID eventId){

        EventEntity event = this.eventRepository.findById(eventId).orElseThrow();

        ProposalEntity proposalEntity = new ProposalEntity(
                event,
                proposalDTO.startDate()
        );

        return this.proposalRepository.save(proposalEntity);
    }

//    public void deleteProposal(UUID id) {
//        this.proposalRepository.deleteById(id);
//    }

    public ProposalRo toProposalRo(ProposalEntity proposalEntity) {
        return new ProposalRo(
                proposalEntity.getId(),
                proposalEntity.getStartDate()
        );
    }

}

