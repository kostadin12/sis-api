package com.kostadin.sis.subscriptions.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Objects;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "SUBSCRIPTIONS_ID")
public class SubscriptionId {
    @Column(name = "USER_ID")
    private long userId;

    @Column(name = "SUBSCRIBER_ID")
    private long subscriberId;

    @Override
    public int hashCode() {
        return Objects.hash(userId, subscriberId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        SubscriptionId that = (SubscriptionId) o;
        return userId == that.userId &&
                subscriberId == that.subscriberId;
    }
}
