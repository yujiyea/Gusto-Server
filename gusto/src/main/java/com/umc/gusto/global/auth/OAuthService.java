package com.umc.gusto.global.auth;

import com.umc.gusto.domain.user.SocialRepository;
import com.umc.gusto.domain.user.UserService;
import com.umc.gusto.domain.user.entity.Social;
import com.umc.gusto.domain.user.model.response.FirstLogInResponse;
import com.umc.gusto.global.auth.model.CustomOAuth2User;
import com.umc.gusto.global.auth.model.OAuthAttributes;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthService extends DefaultOAuth2UserService {
    private final SocialRepository socialRepository;
    private final UserService userService;

    // 유저 불러오기 - 해당 유저의 security context가 저장됨
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // provider - string to enum으로 변환
        Social.SocialType provider = Social.SocialType.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
        String userNameAttribute = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint()
                .getUserNameAttributeName();

        OAuthAttributes oAuthAttributes = OAuthAttributes.of(provider, userNameAttribute, oAuth2User.getAttributes());

        Optional<Social> socialInfo = socialRepository.findBySocialTypeAndProviderId(provider, oAuthAttributes.getId());

        Social info;

        if(socialInfo.isEmpty()) {
             info = socialRepository.save(Social.builder()
                     .socialType(provider)
                     .providerId(oAuthAttributes.getId())
                     .socialStatus(Social.SocialStatus.WAITING_SIGN_UP)
                     .temporalToken(UUID.randomUUID())
                     .build());
        } else {
            info = socialInfo.get();
        }

        if(info.getSocialStatus() == Social.SocialStatus.DISCONNECTED) {
            // TODO: error throw
        }

        return CustomOAuth2User.builder()
                .delegate(oAuth2User)
                .oAuthAttributes(oAuthAttributes)
                .socialInfo(info)
                .build();
    }

    public FirstLogInResponse generateFirstLogInRes(OAuthAttributes oAuthAttributes) {
        String nickname = oAuthAttributes.getNickname();

        if(nickname == null) {
            nickname = userService.generateRandomNickname();
        }

        return new FirstLogInResponse(nickname,
                oAuthAttributes.getProfileImg(),
                oAuthAttributes.getGender().name(),
                oAuthAttributes.getAge().name());
    }
}
