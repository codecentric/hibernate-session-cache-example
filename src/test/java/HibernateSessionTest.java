import entities.SomeEntity;
import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryCountHolder;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;
import util.HibernateUtil;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class HibernateSessionTest {
    @Test
    void sessionCacheIsInterTransactional() {
        try (final Session session = HibernateUtil.getSession()) {

            final Transaction transactionA = session.beginTransaction();
            final int entityId = 1;
            createEntity(session, entityId);
            transactionA.commit();

            final Transaction transactionB = session.beginTransaction();
            // clear cache after entity creation, otherwise we would have no select at all
            session.clear();
            // intended only select
            final Date entityCreationDateA = readEntityCreationDate(session, entityId);
            transactionB.commit();

            final Transaction transactionC = session.beginTransaction();
            // another read, but no further select expected, although we opened a different transaction context
            final Date entityCreationDateB = readEntityCreationDate(session, entityId);
            transactionC.commit();

            assertThat(entityCreationDateB).isEqualTo(entityCreationDateA);

            final QueryCount grandTotal = QueryCountHolder.getGrandTotal();
            assertThat(grandTotal.getInsert()).isEqualTo(1);
            assertThat(grandTotal.getSelect()).isEqualTo(1);
        }

    }

    private Date readEntityCreationDate(final Session session, final int entityId) {
        return session.load(SomeEntity.class, entityId).getCreatedDate();
    }

    private void createEntity(final Session session, final int entityId) {
        session.save(new SomeEntity(entityId));
    }

}
