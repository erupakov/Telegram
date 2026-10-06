package org.telegram.divo.dal.network

/**
 * True when `/auth/telegram-link` was rejected because no proof was sent: teamgram didn't issue
 * `divo_link_proof` (link secret not configured, 2FA, no phone), not a problem with the user's data.
 */
fun DivoResult<*>.isLinkProofMissing(): Boolean =
    this is DivoResult.HttpError && code == 422 && body?.errors?.containsKey("proof") == true
