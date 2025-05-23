package soundtribe.soundtribeusers.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import soundtribe.soundtribeusers.models.enums.Rol;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@ToString(exclude = {"seguidos", "seguidores"})
@NoArgsConstructor
@Entity
@Audited
@Builder
@Table(name = "usuarios")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String password;

    private String descripcion;

    @Column(unique = true)
    private String slug;


    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Rol rol;

    private boolean enabled;

    @ManyToOne
    @JoinColumn(name = "foto_id", referencedColumnName = "id")
    private FotoEntity foto;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @OneToMany(mappedBy = "follower", cascade = CascadeType.REMOVE)
    private List<FollowerFollowedEntity> seguidos;

    @OneToMany(mappedBy = "followed", cascade = CascadeType.REMOVE)
    private List<FollowerFollowedEntity> seguidores;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    private TokenEntity token;


}