/**
 * 
 */
package org.metaborg.sunshine.pipeline.diff;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class MultiDiff<T> extends AbstractCollection<Diff<T>> implements Iterable<Diff<T>> {

    private final Set<Diff<T>> additions = new HashSet<Diff<T>>();
    private final Set<Diff<T>> deletions = new HashSet<Diff<T>>();
    private final Set<Diff<T>> modifications = new HashSet<Diff<T>>();

    public Set<Diff<T>> getDiff(DiffKind kind) {
        switch(kind) {
            case ADDITION:
                return additions;
            case DELETION:
                return deletions;
            case MODIFICATION:
                return modifications;
            default:
                throw new UnsupportedOperationException("Unsupported DiffKind kind: " + kind);
        }
    }

    public Collection<T> values() {
        final Collection<T> values = new HashSet<T>();
        final Iterator<T> iter = valuesIter();
        while(iter.hasNext()) {
            values.add(iter.next());
        }
        return values;
    }

    public Iterator<T> valuesIter() {
        return new Iterator<T>() {
            private final Iterator<Diff<T>> boxIterator = iterator();

            @Override public boolean hasNext() {
                return boxIterator.hasNext();
            }

            @Override public T next() {
                return boxIterator.next().getPayload();
            }

            @Override public void remove() {
                throw new UnsupportedOperationException("Remove unsupported on MultiDiffSpec iterator");
            }

        };
    }

    @Override public Iterator<Diff<T>> iterator() {
        return new Iterator<Diff<T>>() {
            private Iterator<Diff<T>> additionsIter = additions.iterator();
            private Iterator<Diff<T>> deletionsIter = deletions.iterator();
            private Iterator<Diff<T>> modificationsIter = modifications.iterator();

            @Override public boolean hasNext() {
                return additionsIter.hasNext() || deletionsIter.hasNext() || modificationsIter.hasNext();
            }

            @Override public Diff<T> next() {
                if(additionsIter.hasNext())
                    return additionsIter.next();
                else if(deletionsIter.hasNext())
                    return deletionsIter.next();
                else
                    return modificationsIter.next();
            }

            @Override public void remove() {
                throw new UnsupportedOperationException("Remove unsupported on MultiDiffSpec iterator");
            }
        };
    }

    @Override public boolean add(Diff<T> diff) {
        switch(diff.getDiffKind()) {
            case ADDITION:
                return additions.add(diff);
            case DELETION:
                return deletions.add(diff);
            case MODIFICATION:
                return modifications.add(diff);
            default:
                throw new UnsupportedOperationException("Unsupported DiffKind kind: " + diff.getDiffKind());
        }
    }

    @Override public int size() {
        return additions.size() + deletions.size() + modifications.size();
    }

}
