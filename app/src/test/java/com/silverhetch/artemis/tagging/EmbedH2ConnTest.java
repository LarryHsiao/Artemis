package com.silverhetch.artemis.tagging;


import com.larryhsiao.juno.AllFiles;
import com.larryhsiao.juno.FakeDataConn;
import com.larryhsiao.juno.QueriedAFiles;
import com.larryhsiao.juno.TagDbConn;
import com.larryhsiao.juno.h2.EmbedH2Conn;
import com.larryhsiao.juno.h2.MemoryH2Conn;
import com.silverhetch.clotho.source.ConstSource;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;

/**
 * Test for {@link EmbedH2Conn}
 */
public class EmbedH2ConnTest {
    /**
     * Check if we can connect to the embed database.
     */
    @Test
    public void simple() throws Exception {
        try (final Connection conn = new FakeDataConn(new TagDbConn(new MemoryH2Conn())).value()) {
            Assert.assertEquals(
                    2,
                    new QueriedAFiles(new AllFiles(new ConstSource<>(conn))).value().size()
            );
        }
    }
}