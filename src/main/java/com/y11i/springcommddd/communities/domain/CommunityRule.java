package com.y11i.springcommddd.communities.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CommunityRule implements ValueObject {
    @Column(name = "rule_title", length = 100, nullable = false)
    private String title;

    @Column(name = "rule_description", length = 512, nullable = false)
    private String description;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected CommunityRule() {}

    public CommunityRule(String title, String description, int displayOrder) {
        if(title==null || title.isBlank())             throw new IllegalArgumentException("title cannot be null or empty");
        if(title.length() > 100)                       throw new IllegalArgumentException("Title exceeds 100 characters");
        if(description==null || description.isBlank()) throw new IllegalArgumentException("description cannot be null or empty");
        if(description.length() > 512)                throw new IllegalArgumentException("Description exceeds 512 characters");

        this.title = title;
        this.description = description;
        this.displayOrder = displayOrder;
    }

    public String title()        { return this.title; }
    public String description()  { return this.description; }
    public int    displayOrder() { return this.displayOrder; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommunityRule that)) return false;
        return displayOrder == that.displayOrder
                && title.equals(that.title)
                && description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(title, description, displayOrder);
    }
}
