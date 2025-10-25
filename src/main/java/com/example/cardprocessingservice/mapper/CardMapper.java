package com.example.cardprocessingservice.mapper;

import com.example.cardprocessingservice.dto.CardCreateRequest;
import com.example.cardprocessingservice.dto.CardResponse;
import com.example.cardprocessingservice.model.entity.Card;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CardMapper {

    @Mapping(target = "card_id", source = "id")
    @Mapping(target = "user_id", source = "authUser.id")
    @Mapping(target = "balance", expression = "java(card.getBalance().longValue())")
    CardResponse toResponse(Card card);

    @Mapping(target = "balance",
            expression = "java(request.getInitial_amount() != 0L ? java.math.BigDecimal.valueOf(request.getInitial_amount()) : java.math.BigDecimal.ZERO)")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authUser", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateCardFromRequest(CardCreateRequest request, @MappingTarget Card card);
}
