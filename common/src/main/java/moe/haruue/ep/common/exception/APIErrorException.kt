package moe.haruue.ep.common.exception

import moe.haruue.ep.common.data.api.APIError

/** re-wrap [APIError] as [RuntimeException] */
class APIErrorException(val err: APIError): RuntimeException()
