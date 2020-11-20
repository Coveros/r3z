package coverosR3z.misc

class FileReader {
    companion object {

        /**
         * Read in template file as a string
         */
        fun read(filename: String) : String? {
            val foo = this::class.java.classLoader.getResource(filename)
                    ?: return null
            return foo.readBytes().toString(Charsets.UTF_8)
        }

        /**
         * Returns true if the requested file exists in resources
         */
        fun exists(filename: String) : Boolean {
            return this::class.java.classLoader.getResource(filename) != null
        }

        fun readBytes(filename: String): ByteArray {
            val foo = this::class.java.classLoader.getResource(filename)
                ?: return null
            return foo.readBytes()
        }
    }
}