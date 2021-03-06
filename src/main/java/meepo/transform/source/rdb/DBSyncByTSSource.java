package meepo.transform.source.rdb;

import meepo.transform.channel.RingbufferChannel;
import meepo.transform.config.TaskContext;
import meepo.util.Util;
import meepo.util.dao.BasicDao;
import org.apache.commons.lang3.Validate;

/**
 * Created by peiliping on 17-3-10.
 */
public class DBSyncByTSSource extends DBSyncByIdSource {

    private long delay;

    private long now;

    public DBSyncByTSSource(String name, int index, int totalNum, TaskContext context, RingbufferChannel rb) {
        super(name, index, totalNum, context, rb);
        Validate.notBlank(context.get("primaryKeyName"));
        super.stepSize = context.getInteger("stepSize", 60000);
        this.delay = context.getLong("delay", 5000L);
        this.now = System.currentTimeMillis();
        super.start = context.getLong("start", this.now - this.delay);
        super.currentPos = super.start;
    }

    @Override
    public void work() {
        super.startEnd = BasicDao.autoGetStartEndPoint(super.dataSource, super.tableName, super.primaryKeyName, super.rollingSql);
        this.now = System.currentTimeMillis();
        super.tmpEnd = Math.min(super.currentPos + super.stepSize, Math.min(this.now - this.delay, super.startEnd.getRight()));
        if (super.tmpEnd == super.currentPos) {
            super.currentPos = this.now - this.delay;
            Util.sleep(1);
            return;
        }
        if (executeQuery()) {
            super.currentPos = super.tmpEnd;
        }
    }
}
