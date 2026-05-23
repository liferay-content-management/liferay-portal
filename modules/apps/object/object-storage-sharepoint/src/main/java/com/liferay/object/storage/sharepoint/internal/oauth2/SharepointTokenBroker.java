/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.internal.oauth2;

import com.liferay.object.storage.sharepoint.configuration.SharepointConfiguration;

import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.AuthorizationRequestUrlParameters;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.Prompt;
import com.microsoft.aad.msal4j.ResponseMode;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

/**
 * @author Jürgen Kappler
 */
public class SharepointTokenBroker {

	public SharepointTokenBroker(
		SharepointConfiguration sharepointConfiguration) {

		_sharepointConfiguration = sharepointConfiguration;
	}

	public String getAuthorizationRequestUrl(
			String nonce, String redirectURI, String state)
		throws MalformedURLException {

		ConfidentialClientApplication confidentialClientApplication =
			_getConfidentialClientApplication();

		URL url = confidentialClientApplication.getAuthorizationRequestUrl(
			AuthorizationRequestUrlParameters.builder(
				redirectURI,
				Collections.singleton(_sharepointConfiguration.scope())
			).responseMode(
				ResponseMode.QUERY
			).prompt(
				Prompt.SELECT_ACCOUNT
			).state(
				state
			).nonce(
				nonce
			).build());

		return url.toString();
	}

	public IAuthenticationResult requestAccessToken(
			String code, String redirectURI)
		throws ExecutionException, InterruptedException, MalformedURLException,
			   URISyntaxException {

		ConfidentialClientApplication confidentialClientApplication =
			_getConfidentialClientApplication();

		return confidentialClientApplication.acquireToken(
			AuthorizationCodeParameters.builder(
				code, new URI(redirectURI)
			).scopes(
				Collections.singleton(_sharepointConfiguration.scope())
			).build()
		).get();
	}

	private ConfidentialClientApplication _getConfidentialClientApplication()
		throws MalformedURLException {

		return ConfidentialClientApplication.builder(
			_sharepointConfiguration.clientId(),
			ClientCredentialFactory.createFromSecret(
				_sharepointConfiguration.clientSecret())
		).authority(
			"https://login.microsoftonline.com/" +
				_sharepointConfiguration.tenantId()
		).build();
	}

	private final SharepointConfiguration _sharepointConfiguration;

}