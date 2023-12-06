package combat.squad.event;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import combat.squad.proposal.ProposalEntity;
import combat.squad.user.UserEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "events")
public class EventEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;

    private String name;

    private String description;

    @OneToOne
    private ProposalEntity finalProposal;

    private String location;

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "event")
    private List<ProposalEntity> eventProposals;

    @ManyToOne
    @JsonBackReference
    private UserEntity creator;

    @CreatedDate
    private Date created;

    public EventEntity(String name, String description, ProposalEntity finalProposal,String location, UserEntity creator, List<ProposalEntity> eventProposals) {
        this.name = name;
        this.description = description;
        this.finalProposal = finalProposal;
        this.location = location;
        this.creator = creator;
        this.eventProposals = eventProposals;
    }

    public void addProposal(ProposalEntity proposal) {
        this.eventProposals.add(proposal);
    }

}
