package coverosR3z.server.types

interface PostEndpoint : Api {

    /**
     * Note you will need to consider authentication properly
     * here.  for example:
     * [coverosR3z.server.utility.doGETAuthAndUnauth]
     */
    fun handlePost(sd : ServerData) : PreparedResponseData

    /**
     * What separates a POST from a GET is the data we are sent.
     * Here, you specify the keys for the data you expect to receive.
     *
     * For example, if you require the data to include username and password,
     * you include that here.
     */
    val requiredInputs: Set<String>


}