/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.integration;

import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.ogm.driver.Drivers;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.neo4j.ogm.session.transaction.LongTransaction;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

import static org.junit.Assert.assertEquals;

/**
 * @author Michal Bachman
 */
public class TransactionRequestHandlerTest
{

    @ClassRule
    public static Neo4jIntegrationTestRule neo4jRule = new Neo4jIntegrationTestRule();

    private Session session;


    @Test
    public void testCreateLongTransaction() {

        TransactionManager txRequestHandler = new TransactionManager(Drivers.HTTP, neo4jRule.url());
        try (Transaction tx = txRequestHandler.openTransaction(null)) {
            assertEquals(Transaction.Status.OPEN, tx.status());
        }
    }

    @Test
    public void testCreateConcurrentTransactions() {

        TransactionManager txRequestHandler = new TransactionManager(Drivers.HTTP, neo4jRule.url());

        // note that the try-with-resources implies these transactions are nested, but they are in fact independent
        try (Transaction tx1 = txRequestHandler.openTransaction(null)) {
            try (Transaction tx2 = txRequestHandler.openTransaction(null)) {
                assertEquals(Transaction.Status.OPEN, tx1.status());
                assertEquals(Transaction.Status.OPEN, tx2.status());
            }
        }
    }


    @Test(expected = ResultProcessingException.class)
    public void shouldDetectErrorsOnCommitOfLongRunningTransaction() {
        SessionFactory sessionFactory = new SessionFactory("");
        session = sessionFactory.openSession(neo4jRule.url());
        Transaction tx = session.beginTransaction();
        session.query("GARBAGE", Utils.map());
        tx.commit();
    }

    @Test(expected = ResultProcessingException.class)
    public void shouldDetectErrorsOnCommitOfNonExistentTransaction() {
        TransactionManager txRequestHandler = new TransactionManager(Drivers.HTTP, neo4jRule.url());
        Transaction tx = new LongTransaction(null, neo4jRule.url(), txRequestHandler);
        tx.commit();
    }


}
