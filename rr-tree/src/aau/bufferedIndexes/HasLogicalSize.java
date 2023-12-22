package aau.bufferedIndexes;

/**
 * Classes that implement this interface have notion of logical size, that is, size of contained data as
 * programmer sees it.  Physical size, for example, size taken by Java VM for object instances is not considered.
 * If an instance of implementing class contains object references, then only reference size is considered and not the
 * size of the referred object (i.e. shallow size)
 */
public interface HasLogicalSize {
}
