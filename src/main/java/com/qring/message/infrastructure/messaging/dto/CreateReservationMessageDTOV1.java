package com.qring.message.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationMessageDTOV1 {

    private User user;
    private Reservation reservation;
    private Queue queue;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {

        private Long userId;
        private String slackEmail;
        private String username;

        public static User from(Long userId, String slackEmail, String username) {
            return User.builder()
                    .userId(userId)
                    .slackEmail(slackEmail)
                    .username(username)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reservation {

        private Long id;
        private int headCount;
        private Restaurant restaurant;

        public static Reservation from(Long id, int headCount, Restaurant restaurant) {
            return Reservation.builder()
                    .id(id)
                    .headCount(headCount)
                    .restaurant(restaurant)
                    .build();
        }

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Restaurant {

            private Long id;
            private String name;
            private String tel;

            public static Restaurant from(Long id, String restaurantName, String restaurantTel) {
                return Restaurant.builder()
                        .id(id)
                        .name(restaurantName)
                        .tel(restaurantTel)
                        .build();
            }
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Queue {
        private int sequence;

        public static Queue from(int sequence) {
            return Queue.builder()
                    .sequence(sequence)
                    .build();
        }
    }
}