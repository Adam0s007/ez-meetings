package combat.squad.event;

import combat.squad.auth.UserRo;
import combat.squad.proposal.ProposalDto;
import combat.squad.proposal.ProposalEntity;
import combat.squad.proposal.ProposalRo;
import combat.squad.proposal.ProposalService;
import combat.squad.auth.UserEntity;
import combat.squad.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private final ProposalService proposalService;

    @Autowired
    public EventService(EventRepository eventRepository, UserRepository userRepository, ProposalService proposalService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.proposalService = proposalService;
    }

    public List<EventEntity> getEvents() {
        return this.eventRepository.findAll();
    }

    public EventEntity getEventById(UUID id) {
        return this.eventRepository.findById(id).orElseThrow();
    }


    public EventRo getEventDetails(String userEmail, UUID eventId) {

        UserEntity user = this.userRepository.findByEmail(userEmail);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        if (user.getEvents().stream().noneMatch(event -> event.getId().equals(eventId))) {
            throw new IllegalArgumentException("User is not a participant of this event");
        }

        EventEntity event = this.eventRepository.findById(eventId).orElseThrow();

        return this.toEventRo(event, true);
    }


    public List<EventRo> getEventsByUser(String userEmail) {

        UserEntity user = this.userRepository.findByEmail(userEmail);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        return user.getEvents().stream()
                .map(event -> toEventRo(event, false))
                .collect(Collectors.toList());
    }

    public EventRo createEvent(String userEmail, EventDto eventDto){

        UserEntity user = this.userRepository.findByEmail(userEmail);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        EventEntity event = new EventEntity(
                eventDto.name(),
                eventDto.description(),
                null,
                eventDto.location(),
                user,
                new ArrayList<>()
        );

        List<ProposalDto> proposals = eventDto.eventProposals();

        if (proposals.isEmpty()) {
            throw new IllegalArgumentException("Event must have at least one proposal");
        }

        event = this.eventRepository.save(event);

        List<ProposalEntity> proposalEntities = new ArrayList<>();

        for (ProposalDto proposalDTO : proposals) {
            ProposalEntity proposalEntity = this.proposalService.createProposal(proposalDTO, event.getId());
            proposalEntities.add(proposalEntity);
        }

        event.getEventProposals().addAll(proposalEntities);
        this.eventRepository.save(event);

        return this.toEventRo(event, true);
    }

    public EventRo toEventRo(EventEntity event, Boolean showProposals) {

        List<ProposalEntity> proposals = event.getEventProposals();

        Optional<List<ProposalRo>> proposalRoList = showProposals ?
                Optional.of(proposals.stream()
                        .map(proposalService::toProposalRo)
                        .collect(Collectors.toList())) : Optional.empty();

        return new EventRo(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getLocation(),
                proposalRoList
        );
    }

}
