package gabri.dev.javaspringcompose.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Audited
@Table(
        name = "follower_x_followed",
        uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "followed_id"})
)
public class FollowerFollowedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "follower_id", referencedColumnName = "id")
    private UserEntity follower;

    @ManyToOne(optional = false)
    @JoinColumn(name = "followed_id", referencedColumnName = "id")
    private UserEntity followed;

    @CreationTimestamp
    @Column(name = "followed_at", updatable = false)
    private LocalDateTime followedAt;
}
