package com.y11i.springcommddd.communities.domain;

import com.y11i.springcommddd.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "communities",
        uniqueConstraints = @UniqueConstraint(name="uk_communities_name_key", columnNames={"name_key"}))
@EntityListeners(AuditingEntityListener.class)
@Access(AccessType.FIELD)
public class Community implements AggregateRoot {

    @EmbeddedId
    private CommunityId communityId;

    @Embedded
    private CommunityName communityName;

    @Embedded
    private CommunityNameKey communityNameKey;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false, length=20)
    private CommunityStatus status;

    @CreatedDate
    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name="updated_at", nullable=false)
    private Instant updatedAt;

    @Version
    private long version;

    protected Community() {}

    private Community(CommunityName communityName) {
        this.communityId = CommunityId.newId();
        this.communityName = Objects.requireNonNull(communityName);
        this.communityNameKey = new CommunityNameKey(communityName.value());
        this.status = CommunityStatus.ACTIVE;
    }

    /**
     * Create a new Community
     * @param communityName new name for the community
     * @return new Community object
     */
    public static Community create(String communityName) {
        return new Community(new CommunityName(communityName));
    }

    /**
     * Renames unarchived communities
     * @param newCommunityName A new name for the Community
     */
    public void rename(String newCommunityName) {
        ensureNotArchived();
        this.communityName = new CommunityName(newCommunityName);
        this.communityNameKey = new CommunityNameKey(newCommunityName);
    }

    /**
     * Archive Community. Acts as soft delete
     */
    public void archive() {
        this.status = CommunityStatus.ARCHIVED;
    }

    /**
     * Restores communities from archived status
     */
    public void restore() {
        if (status != CommunityStatus.ARCHIVED) throw new IllegalStateException("Only ARCHIVED can be restored");
        this.status = CommunityStatus.ACTIVE;
    }

    /**
     * Makes sure a community is not archived
     */
    private void ensureNotArchived(){
        if (status == CommunityStatus.ARCHIVED) throw new IllegalStateException("Archived community cannot be renamed");
    }

    /** Accessors (read-only) */
    public CommunityId communityId(){ return communityId; }
    public CommunityName communityName(){ return communityName; }
    public CommunityNameKey nameKey(){ return communityNameKey; }
    public CommunityStatus status(){ return status; }
    public Instant createdAt(){ return createdAt; }
    public Instant updatedAt(){ return updatedAt; }
    public long version(){ return version; }
}
