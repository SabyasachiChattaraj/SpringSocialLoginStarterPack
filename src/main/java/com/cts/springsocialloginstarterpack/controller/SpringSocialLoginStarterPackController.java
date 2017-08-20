package com.cts.springsocialloginstarterpack.controller;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.scribe.builder.api.GoogleApi;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.google.api.impl.GoogleTemplate;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.linkedin.api.LinkedIn;
import org.springframework.social.linkedin.connect.LinkedInConnectionFactory;
import org.springframework.social.oauth1.AuthorizedRequestToken;
import org.springframework.social.oauth1.OAuth1Operations;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import com.cts.springsocialloginstarterpack.util.FacebookCustomApi;
import com.cts.springsocialloginstarterpack.util.OAuthServiceProvider;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.util.PortalUtil;

@Controller
@RequestMapping("VIEW")
public class SpringSocialLoginStarterPackController {
	
	private static final String FACEBOOK = "facebook";
	private static final String LINKEDIN = "linkedin";
	private static final String TWITTER = "twitter";
	private static final String GOOGLE = "google";
	private static final String LOGIN = "login";
	private static final String MAIN_VIEW = "view";
	private static final String DASHBOARD = "loggedin";
	private static final String CALLBACK_FOR = "callback_for";
	private static final String OAUTH_TOKEN = "oauth_token";
	private static final String OAUTH_VERIFIER = "oauth_verifier";
	
	
	@Autowired
	private ConnectionFactoryRegistry connectionFactoryRegistry;

	@Autowired
	private OAuth1Parameters oAuth1Parameters;

	@Value("${app.config.oauth.twitter.callback}")
	private String callback;
	
	@Value("${app.config.oauth.twitter.apikey}")
	private String apiKey;
	
	@Value("${app.config.oauth.twitter.apisecret}")
	private String apiSecret;
	
	@Autowired
	private OAuth2Parameters linkedInOAuth2Parameters;
	
	@Autowired
	private OAuth2Parameters facebookOAuth2Parameters;
	
	@Autowired
	private OAuth2Parameters googleOAuth2Parameters;
	
	@Autowired
	@Qualifier("linkedInServiceProvider")
	private OAuthServiceProvider<LinkedInApi> linkedInServiceProvider;
	
	@Autowired
	@Qualifier("facebookServiceProvider")
	private OAuthServiceProvider<FacebookCustomApi> facebookServiceProvider;
	
	@Autowired
	@Qualifier("googleServiceProvider")
	private OAuthServiceProvider<GoogleApi> googleServiceProvider;
	
	@RenderMapping
	public String handleRenderRequest(Model model,RenderRequest renderRequest, RenderResponse renderResponse) throws Exception {
		HttpServletRequest httpServletRequest=PortalUtil.getHttpServletRequest(renderRequest);
		httpServletRequest=PortalUtil.getOriginalServletRequest(httpServletRequest);
		String callbackFor=httpServletRequest.getParameter(CALLBACK_FOR);
		String oauthToken=httpServletRequest.getParameter(OAUTH_TOKEN);
		String oauthVerifier=httpServletRequest.getParameter(OAUTH_VERIFIER);
		System.out.println("oauthToken :"+oauthToken);
		System.out.println("oauthVerifier :"+oauthVerifier);
		if(Validator.isNotNull(callbackFor)&&callbackFor.equals(TWITTER)){
			TwitterConnectionFactory twitterConnectionFactory = (TwitterConnectionFactory) connectionFactoryRegistry
					.getConnectionFactory(TWITTER);
			OAuth1Operations oauthOperations = twitterConnectionFactory
					.getOAuthOperations();
			OAuthToken requestToken =  new OAuthToken(oauthToken,null);
			OAuthToken accessToken=oauthOperations.exchangeForAccessToken(new AuthorizedRequestToken(requestToken, oauthVerifier), oAuth1Parameters);
			String consumerKey = apiKey; // The application's consumer key
			String consumerSecret = apiSecret; // The application's consumer secret
			String accessTokenValue = accessToken.getValue();
			String accessTokenSecret = accessToken.getSecret();
			//Connection<Twitter> connection = twitterConnectionFactory.createConnection( accessToken );
			Twitter twitter = new TwitterTemplate( consumerKey, consumerSecret, accessTokenValue, accessTokenSecret );
			String jsonResponse=twitter.restOperations().getForObject("https://api.twitter.com/1.1/account/verify_credentials.json?include_email=true", String.class);
			JSONObject jsonObject=JSONFactoryUtil.createJSONObject(jsonResponse);
			System.out.println("Profile Info with Email: "+ jsonObject.getString("email"));
			model.addAttribute("email", jsonObject.getString("email"));
			return DASHBOARD;
		}else if(Validator.isNotNull(callbackFor)&&callbackFor.equals(LINKEDIN)){
			OAuthService oAuthService = linkedInServiceProvider.getService();
			String code=httpServletRequest.getParameter("code");
			String state=httpServletRequest.getParameter("state");
			LinkedInConnectionFactory linkedInConnectionFactory = (LinkedInConnectionFactory) connectionFactoryRegistry.getConnectionFactory(LINKEDIN);
			OAuth2Operations oauthOperations = linkedInConnectionFactory
					.getOAuthOperations();
			AccessGrant accessGrant=oauthOperations.exchangeForAccess(code, null, linkedInOAuth2Parameters);
			/*Verifier verifier = new Verifier(code);
			Token accessToken = oAuthService
					.getAccessToken(Token.empty(), verifier);*/
			Connection<LinkedIn> connection = linkedInConnectionFactory.createConnection(accessGrant);
			model.addAttribute("email", connection.fetchUserProfile().getEmail());
			return DASHBOARD;
		}else if(Validator.isNotNull(callbackFor)&&callbackFor.equals(FACEBOOK)){
			OAuthService oAuthService = facebookServiceProvider.getService();
			String code=httpServletRequest.getParameter("code");
			String state=httpServletRequest.getParameter("state");
			FacebookConnectionFactory facebookConnectionFactory = (FacebookConnectionFactory) connectionFactoryRegistry.getConnectionFactory(FACEBOOK);
			/*OAuth2Operations oauthOperations = facebookConnectionFactory.getOAuthOperations();
			AccessGrant accessGrant=oauthOperations.exchangeForAccess(code, null, null);*/
			Verifier verifier = new Verifier(code);
			Token accessToken = oAuthService.getAccessToken(Token.empty(), verifier);
			//Connection<Facebook> connection = facebookConnectionFactory.createConnection(accessGrant);
			Facebook facebook=new FacebookTemplate(accessToken.getToken());
			String jsonResponse=facebook.restOperations().getForObject("https://graph.facebook.com/me?fields=id,email", String.class); 
			JSONObject jsonObject=JSONFactoryUtil.createJSONObject(jsonResponse);
			System.out.println("Profile Info with Email: "+ jsonObject.getString("email"));
			model.addAttribute("email", jsonObject.getString("email"));
			return DASHBOARD;
		}else if(Validator.isNotNull(callbackFor)&&callbackFor.equals(GOOGLE)){
			OAuthService oAuthService = googleServiceProvider.getService();
			String code=httpServletRequest.getParameter("code");
			String state=httpServletRequest.getParameter("state");
			GoogleConnectionFactory googleConnectionFactory = (GoogleConnectionFactory) connectionFactoryRegistry.getConnectionFactory(GOOGLE);
			OAuth2Operations oauthOperations = googleConnectionFactory.getOAuthOperations();
			AccessGrant accessGrant=oauthOperations.exchangeForAccess(code, "http://127.0.0.1:9001/web/guest/home?callback_for=google", null);
			/*Connection<Google> connection = googleConnectionFactory.createConnection(accessGrant);*/
			/*Verifier verifier = new Verifier(code);
			Token accessToken = oAuthService.getAccessToken(oAuthService.getRequestToken(), verifier);
			Google google=new GoogleTemplate(accessToken.getToken());*/
			/*System.out.println("accessGrant"+accessGrant.getAccessToken());*/
			GoogleTemplate google=new GoogleTemplate((String)renderRequest.getParameter("access_token"));
			System.out.println("isAuthorized "+google.isAuthorized());
			Object jsonResponse=google.getRestTemplate().getForObject("https://www.googleapis.com/plus/v1/people/me", Object.class);
			//JSONObject jsonObject=JSONFactoryUtil.createJSONObject(jsonResponse.toString());
			model.addAttribute("email",jsonResponse.toString());
			return DASHBOARD;
		}
		return MAIN_VIEW;
	}

	@ActionMapping(params=LOGIN+StringPool.EQUAL+TWITTER)
	public void authenticateViaTwitter(ActionRequest actionRequest, ActionResponse actionResponse, Model model) throws Exception {
		TwitterConnectionFactory twitterConnectionFactory = (TwitterConnectionFactory) connectionFactoryRegistry
				.getConnectionFactory(TWITTER);
		OAuth1Operations oauthOperations = twitterConnectionFactory
				.getOAuthOperations();
		
		OAuthToken oAuthToken = oauthOperations.fetchRequestToken(callback,
				oAuth1Parameters);
		String authorizeUrl = oauthOperations.buildAuthorizeUrl(
				oAuthToken.getValue(), oAuth1Parameters);
		
		/*String authorizeUrl = oauthOperations.buildAuthenticateUrl(
				oAuthToken.getValue(), oAuth1Parameters);*/
		/*RedirectView redirectView = new RedirectView(authorizeUrl, true, true,
				true);*/
		System.out.println("authorizeUrl :"+authorizeUrl);
		actionResponse.sendRedirect(authorizeUrl);
	}

	@ActionMapping(params=LOGIN+StringPool.EQUAL+LINKEDIN)
	public void authenticateViaLinkedIn(ActionRequest actionRequest, ActionResponse actionResponse, Model model) throws Exception {
		
		LinkedInConnectionFactory linkedInConnectionFactory = (LinkedInConnectionFactory) connectionFactoryRegistry.getConnectionFactory(LINKEDIN);
		OAuth2Operations oauthOperations = linkedInConnectionFactory
				.getOAuthOperations();
		linkedInOAuth2Parameters.setState("recivedfromLinkedIntoken");
		String authorizeUrl = oauthOperations.buildAuthorizeUrl(
				GrantType.AUTHORIZATION_CODE, linkedInOAuth2Parameters);
		actionResponse.sendRedirect(authorizeUrl);
		
	}
	
	
	@ActionMapping(params=LOGIN+StringPool.EQUAL+FACEBOOK)
	public void authenticateViaFacebook(ActionRequest actionRequest, ActionResponse actionResponse, Model model) throws Exception {
		FacebookConnectionFactory facebookConnectionFactory = (FacebookConnectionFactory) connectionFactoryRegistry.getConnectionFactory(FACEBOOK);
		OAuth2Operations oauthOperations = facebookConnectionFactory.getOAuthOperations();
		facebookOAuth2Parameters.setState("recivedfromFacebooktoken");
		String authorizeUrl = oauthOperations.buildAuthorizeUrl(GrantType.AUTHORIZATION_CODE, facebookOAuth2Parameters);
		System.out.println("FB authorizeUrl"+authorizeUrl);
		actionResponse.sendRedirect(authorizeUrl);
	}
	
	
	@ActionMapping(params=LOGIN+StringPool.EQUAL+GOOGLE)
	public void authenticateViaGooglePlus(ActionRequest actionRequest, ActionResponse actionResponse, Model model) throws Exception {
		GoogleConnectionFactory googleConnectionFactory = (GoogleConnectionFactory) connectionFactoryRegistry.getConnectionFactory(GOOGLE);
		OAuth2Operations oauthOperations = googleConnectionFactory.getOAuthOperations();
		googleOAuth2Parameters.setState("recivedfromGoogletoken");
		String authorizeUrl = oauthOperations.buildAuthorizeUrl(GrantType.AUTHORIZATION_CODE, googleOAuth2Parameters);
		actionResponse.sendRedirect(authorizeUrl);
		
	}
}
