/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.storage.dom;

public class DOMException extends RuntimeException{
    public DOMException(short code, String message) {
       super(message);
       this.code = code;
    }
    public short   code;

    // ExceptionCode
    /**
     * If index or size is negative, or greater than the allowed value
     */
    public static final short INDEX_SIZE_ERR            = 1;
    /**
     * If the specified range of text does not fit into a DOMString
     */
    public static final short DOMSTRING_SIZE_ERR        = 2;
    /**
     * If any node is inserted somewhere it doesn't belong
     */
    public static final short HIERARCHY_REQUEST_ERR     = 3;
    /**
     * If a node is used in a different document than the one that created it 
     * (that doesn't support it)
     */
    public static final short WRONG_DOCUMENT_ERR        = 4;
    /**
     * If an invalid or illegal character is specified, such as in a name. See 
     * production 2 in the XML specification for the definition of a legal 
     * character, and production 5 for the definition of a legal name 
     * character.
     */
    public static final short INVALID_CHARACTER_ERR     = 5;
    /**
     * If data is specified for a node which does not support data
     */
    public static final short NO_DATA_ALLOWED_ERR       = 6;
    /**
     * If an attempt is made to modify an object where modifications are not 
     * allowed
     */
    public static final short NO_MODIFICATION_ALLOWED_ERR = 7;
    /**
     * If an attempt is made to reference a node in a context where it does 
     * not exist
     */
    public static final short NOT_FOUND_ERR             = 8;
    /**
     * If the implementation does not support the requested type of object or 
     * operation.
     */
    public static final short NOT_SUPPORTED_ERR         = 9;
    /**
     * If an attempt is made to add an attribute that is already in use 
     * elsewhere
     */
    public static final short INUSE_ATTRIBUTE_ERR       = 10;
    /**
     * If an attempt is made to use an object that is not, or is no longer, 
     * usable.
     * @since DOM Level 2
     */
    public static final short INVALID_STATE_ERR         = 11;
    /**
     * If an invalid or illegal string is specified.
     * @since DOM Level 2
     */
    public static final short SYNTAX_ERR                = 12;
    /**
     * If an attempt is made to modify the type of the underlying object.
     * @since DOM Level 2
     */
    public static final short INVALID_MODIFICATION_ERR  = 13;
    /**
     * If an attempt is made to create or change an object in a way which is 
     * incorrect with regard to namespaces.
     * @since DOM Level 2
     */
    public static final short NAMESPACE_ERR             = 14;
    /**
     * If a parameter or an operation is not supported by the underlying 
     * object.
     * @since DOM Level 2
     */
    public static final short INVALID_ACCESS_ERR        = 15;
}
