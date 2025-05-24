package com.kostadin.sis.subscriptions.model;

import com.kostadin.sis.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "SUBSCRIPTIONS")
public class Subscription {
    @EmbeddedId
    private SubscriptionId id;

    @ManyToOne(fetch = LAZY)
    @MapsId("userId")
    private User user;

    @ManyToOne(fetch = LAZY)
    @MapsId("subscriberId")
    private User subscriber;

    public Subscription(User user, User subscriber){
        this.user = user;
        this.subscriber = subscriber;
        this.id = new SubscriptionId(user.getId(), subscriber.getId());
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        return (obj instanceof Subscription other) && id == other.id;
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
