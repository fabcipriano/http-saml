package br.com.facio.labs.http.saml;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fabianocp
 */
public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        AtomicLong tid = new AtomicLong(0);
        LOG.info("Starting Vert.x http server ...");

        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(128));
        HttpServer server = vertx.createHttpServer();
        Router router = createRouterWithSessionHandler(vertx);
        

        router.route().path("/hello/blocking/").blockingHandler(routingContext -> {
            LOG.info("Blocking ...");
            sleep(3000);

            routingContext.next();
        }, false);

        router.route().path("/hello/*").handler(routingContext -> {

            nonBlocking(routingContext, tid);
        });


        server.requestHandler(router).listen(8181);
        LOG.info("Started vert.x !!!");
    }

    private static Router createRouterWithSessionHandler(Vertx vertx) {
        SessionStore store = LocalSessionStore.create(vertx);
        Router router = Router.router(vertx);
        router.route().handler(CookieHandler.create());
        SessionHandler sessionHandler = SessionHandler.create(store);
        // Make sure all requests are routed through the session handler too
        router.route().handler(sessionHandler);
        return router;
    }

    private static void nonBlocking(RoutingContext routingContext, AtomicLong tid) {
        execute(routingContext, tid);
    }

//    private static void blocking(RoutingContext routingContext, AtomicLong tid) {
//        execute(routingContext, tid, 2000);
//    }
//
    private static void execute(RoutingContext routingContext, AtomicLong tid) {
        // This handler will be called for every request
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "text/plain");

        LOG.info("{} - responding path - {} ...", tid.get(), routingContext.normalisedPath());

        // Write to the response and end it
        if ((tid.get() % 3) == 1) {
            LOG.info("{} - ERROR...", tid.get());
            response.setStatusCode(500);
            response.setStatusMessage("Vert.x-Web force a error to test HttpConnection java problem. Hash Error 666 !!!");
        }
        response.end(RESPONSE);
        
        LOG.info("{} - responded.", tid.get());
        long incrementAndGet = tid.incrementAndGet();
        LOG.info("incrementAndGet = {}", incrementAndGet);        
    }
    private static void sleep(int time) {
        if (time <= 0) {
            return;
        }
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            LOG.error("unexpected exception", ex);
        }
    }

    private static final String RESPONSE = "Hello World from Vert.x-Web!!!!!!!!!\n"
            + "wget --post-file=request.xml --header=\"Content-Type:text/xml\" --header=\"SOAPAction:\\\"\\\"\" http://localhost:9000/assetService -O rep.xml\n" +
            "\n" +
            "Eventos de Lentidão apontados pelo nossso sistema de monitoramento em Maio.\n" +
            "Inicio             Fim                 Tempo\n" +
            "May 26, 9:31 pm	   May 26, 9:37 pm	   6m\n" +
            "May 26, 8:42 pm	   May 26, 9:06 pm	   24m\n" +
            "May 20, 10:39 am   May 20, 10:49 am	   10m\n" +
            "May 18, 9:00 pm	   May 18, 9:11 pm	   11m\n" +
            "May 18, 12:56 pm   May 18, 1:02 pm	   6m\n" +
            "May 18, 8:58 am	   May 18, 9:04 am	   6m\n" +
            "May 18, 6:57 am	   May 18, 7:04 am	   7m\n" +
            "May 15, 9:03 am	   May 15, 9:19 am	   16m\n" +
            "May 15, 8:46 am	   May 15, 8:51 am     5m\n" +
            "May 11, 4:33 pm	   May 11, 4:52 pm	   19m\n" +
            "May 11, 2:47 pm	   May 11, 2:52 pm	   5m\n" +
            "May 11, 6:11 am	   May 11, 6:20 am	   9m\n" +
            "May 8, 5:04 pm	   May 8, 5:08 pm	   4m\n" +
            "May 2, 10:57 pm	   May 2, 11:05 pm	   8m\n"+ 
            "Ipsis litteris (diga /ípsis líteris/), “com as mesmas letras”, é uma das muitas expressões usadas na linguagem culta para indicar que alguma coisa está sendo transcrita literalmente, com toda a exatidão: “O texto a seguir é a reprodução ipsis litteris da carta enviada por Stálin em 1946″. Uma expressão similar é ad litteram (“ao pé da letra”), embora esta possa também significar outra nuança de “literalmente”, como se pode ver na frase “O erro dos pesquisadores foi tomar esses provérbios populares ad litteram“.\n" +
"\n" +
"Subindo da letra para a palavra, pode-se empregar o tradicional ipsis verbis (diga /ípsis vérbis/, “com as mesmas palavras”), que também aparece na versão enfática (e impressionante) de ipsissima verba, traduzida mais ou menos como “as mesmíssimas palavras”. \n" +
"\n" +
"Outra derivada do Latim verbum (“palavra”), usada para a mesma finalidade, é verbatim (“palavra por palavra”): “A testemunha conseguiu repetir o diálogo verbatim“. Para ressaltar ainda mais a exatidão do texto, acadêmicos rigorosos criaram o verbatim et literatim, que assegura que a transcrição foi feita “palavra por palavra, letra por letra”. O que mais um leitor poderia querer? Pois não é que alguns exagerados chegaram a um verbatim et literatim et punctatim, que deveria, na cabeça oca deles, indicar que até os sinais de pontuação tinham sido respeitados? O problema é que foram traídos pela rima, uma vez que punctatim, que significa “breve, conciso”, não tem nada a ver com a pontuação. \n" +
"\n" +
"Essa indicação de rigor na transcrição também aparece no mundo real, não-acadêmico, por meio dos populares tintim por tintim, sem tirar nem pôr ou com todos os efes-e-erres.\n";

}
