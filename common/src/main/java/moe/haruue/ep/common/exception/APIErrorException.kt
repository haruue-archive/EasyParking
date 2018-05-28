package moe.haruue.ep.common.exception

import moe.haruue.ep.common.data.api.APIError

/** re-wrap [APIError] as [RuntimeException] */
@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
class APIErrorException(val err: APIError): RuntimeException("APIError: $err")
