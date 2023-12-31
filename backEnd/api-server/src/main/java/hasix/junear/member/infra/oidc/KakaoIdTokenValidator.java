package hasix.junear.member.infra.oidc;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hasix.junear.common.exception.CommonErrorCode;
import hasix.junear.common.exception.CustomException;
import hasix.junear.member.application.OauthMemberInfo;
import hasix.junear.member.domain.OauthProvider;
import hasix.junear.member.infra.jwt.IdTokenResolver;
import hasix.junear.member.infra.oauth.KakaoOAuthProvider;
import hasix.junear.member.infra.oauth.KakaoOauthProperty;
import hasix.junear.member.infra.repository.RedisCacheRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;


@Component
public class KakaoIdTokenValidator extends AbstractIdTokenValidator {

    private final KakaoOAuthProvider kakaoOAuthProvider;
    private final String KAKAO_ID_KEY = "sub";
    private final String KAKAO_NAME_KEY = "nickname";
    private final String KAKAO_PROFILE_IMAGE_KEY = "picture";
    private final RedisCacheRepository redisCacheRepository;
    private final String  KAKAO_JWKS_CACHE_KEY = "kakao-jwks";

    public KakaoIdTokenValidator(KakaoOauthProperty kakaoOauthProperty,
            KakaoOAuthProvider kakaoOAuthProvider, IdTokenResolver idTokenResolver,
            RedisCacheRepository repository) {
        super(kakaoOauthProperty.toIdTokenProperty(), idTokenResolver);
        this.kakaoOAuthProvider = kakaoOAuthProvider;
        this.redisCacheRepository = repository;
    }

    @Override
    List<OidcPublicKey> getOIDCPublicKeys() {
        List<OidcPublicKey> oidcPublicKeys = redisCacheRepository.getOIDCPublicKeys(KAKAO_JWKS_CACHE_KEY);
        if(oidcPublicKeys == null){
            List<OidcPublicKey> publicKeys = kakaoOAuthProvider.getOidcPublicKeys();
            redisCacheRepository.savePublicKey(KAKAO_JWKS_CACHE_KEY,publicKeys);
            oidcPublicKeys = publicKeys;
        }
        return  oidcPublicKeys;
    }

    @Override
    OauthMemberInfo extractMemberInfoFromPayload(Map<String, Object> payload) {
        String oauthId = (String) payload.get(KAKAO_ID_KEY);
        String name = (String) payload.get(KAKAO_NAME_KEY);
        String profileImage = (String) payload.get(KAKAO_PROFILE_IMAGE_KEY);
        if (requireValueIsNull(oauthId, name, profileImage)) {
            throw new CustomException(CommonErrorCode.SERVER_ERROR);
        }

        return OauthMemberInfo.builder()
                              .oauthId(oauthId)
                              .name(name)
                              .profileImageUrl(profileImage)
                              .oauthProvider(OauthProvider.KAKAO)
                              .build();
    }


    /*
     * 해당 예외가 발생하는건 카카오에서 프로퍼티 key 값을 바꾸지 않는 이상은 발생하지 않는다.
     */
    private boolean requireValueIsNull(String oauthId, String name, String profileImage) {
        return oauthId == null || name == null || profileImage == null;
    }
}
