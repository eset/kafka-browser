/*
 * # Copyright (c) 2018-2019 ESET
 * # See LICENSE file for redistribution.
 */
package com.eset.tools.kafkabrowser;


import javax.servlet.annotation.WebServlet;

import com.eset.tools.kafkabrowser.shared.kafka.KafkaConnect;
import com.eset.tools.kafkabrowser.menu.Browser;
import com.eset.tools.kafkabrowser.shared.Config;
import com.eset.tools.kafkabrowser.shared.Shared;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.*;
import com.vaadin.ui.*;

import java.util.Date;
import java.util.Map;

@Theme("mytheme")
@Widgetset("com.eset.tools.kafkabrowser.KafkabrowserWidgetset")
@com.vaadin.annotations.JavaScript({"vaadin://js/jquery.min.js","vaadin://js/jquery-ui.min.js", "vaadin://js/jquery.mark.min.js"})
@StyleSheet("vaadin://css/jquery-ui.css")

public class Kafkabrowser extends UI {
    public Navigator navigator;
    public Label header;
    public Date from;
    public Date to;
    public HorizontalLayout crashTop = new HorizontalLayout();
    public HorizontalLayout footer = new HorizontalLayout();
    public MenuBar menuHeader;
    public static Config conf = null;
    public static int LIMIT = 15;
    public String USERNAME = Shared.ANONYMOUS;
    public String CLUSTER= "";

    public Link linkURL = new Link("URL of this view", new ExternalResource(""));

    @Override
    protected void init(VaadinRequest request) {

        //load conf
        Config.getInstance(); //if Tomcat comment line   @WebServlet(urlPatterns ....


        //layout
        getPage().setTitle("Kafkabrowser");
        VerticalLayout rootLayout = new VerticalLayout();
        setContent(rootLayout);

        //header
        header = new Label("");
        header.setStyleName("contentHeader");
        CssLayout contentLayout = new CssLayout();
        contentLayout.setSizeFull();
        contentLayout.setStyleName("contentBody");

        Label name = new Label("Kafkabrowser");
        name.setStyleName("h2");

        crashTop.setHeight("30px");
        crashTop.addComponent(name);

        //footer
        footer.addComponent(new Label(""));
        footer.addComponent(linkURL);
        rootLayout.addComponents(contentLayout, footer);


        Map<String, String[]> values = request.getParameterMap();

        //display content
        try {
            KafkaConnect.initInstance(Shared.ANONYMOUS, "");
        } catch (Shared.ExceptionUnauthorized unauthorized) {
            unauthorized.printStackTrace();
        }
        contentLayout.addComponent(new Browser(values));

        VaadinSession.getCurrent().getSession().setAttribute("userName",  Shared.getHost(request.getHeader("x-forwarded-for")));
        VaadinSession.getCurrent().getSession().setAttribute("ip", request.getHeader("x-forwarded-for"));
    }


    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)

    @VaadinServletConfiguration(ui = Kafkabrowser.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
