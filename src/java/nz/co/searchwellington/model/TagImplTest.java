package nz.co.searchwellington.model;

import java.util.HashSet;

import junit.framework.TestCase;

public class TagImplTest extends TestCase {
    
    
    public void testIsParentOfTag() throws Exception {
          
        Tag childTag = new TagImpl(1, "child", "Child", null, new HashSet<Tag>(), 0);        
        Tag grandChildTag = new TagImpl(2, "grandchild", "Grand Child", null, new HashSet<Tag>(), 0);
        
        childTag.addChild(grandChildTag);
        grandChildTag.setParent(childTag);
        
        assertTrue(childTag.isParentOf(grandChildTag));
        assertFalse(grandChildTag.isParentOf(childTag));
        
        
        Tag parent = new TagImpl(3, "parent", "Parent", null, new HashSet<Tag>(), 0);
        parent.addChild(childTag);
        childTag.setParent(parent);
        
        assertTrue(parent.isParentOf(childTag));
        assertTrue(parent.isParentOf(grandChildTag));
    }
    
}
