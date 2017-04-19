/*
 * # Copyright (c) 2018-2019 ESET
 * # See LICENSE file for redistribution.
 */

package com.eset.tools.kafkabrowser.menu;

import com.eset.tools.kafkabrowser.Kafkabrowser;
import com.eset.tools.kafkabrowser.shared.kafka.ImplTopics;
import com.eset.tools.kafkabrowser.shared.Config;
import com.eset.tools.kafkabrowser.shared.Shared;
import com.eset.tools.kafkabrowser.shared.Renderer;
import com.eset.tools.kafkabrowser.shared.kafka.InfoObject;
import com.eset.tools.kafkabrowser.shared.kafka.MessageObject;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.*;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.eset.tools.kafkabrowser.shared.kafka.KafkaConnect;
import com.vaadin.ui.components.grid.HeaderCell;
import com.vaadin.ui.components.grid.HeaderRow;
import org.json.simple.JSONValue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.Double;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Browser extends VerticalLayout {

    //variables
    boolean sliderListerner = true;
    boolean init = false;
    int partition = 0;
    String lastLog = "";
    Map<String, String[]> queryParameters;

    //layout
    GridLayout controlArea = new GridLayout(9, 1);
    HorizontalLayout hLayoutControl = new HorizontalLayout();
    Label name = new Label("KAFKA BROWSER");
    ComboBox<String> ui_clusters = new ComboBox("Cluster");
    ComboBox<String> ui_topics = new ComboBox("Topics");
    ComboBox<Integer> ui_partitions = new ComboBox("Partition");
    NativeSelect<Shared.VIEWER> ui_parser = new NativeSelect<>();
    NativeSelect<Integer> ui_limit = new NativeSelect<>();
    TextField ui_offset = new TextField("Offset");
    Grid<MessageObject> grid = new Grid<>(MessageObject.class);
    HeaderRow filterRow = grid.appendHeaderRow();
    Slider slider = new Slider();
    Label ui_log = new Label();
    TextField ui_tb_search = new TextField();
    TextField filterField = new TextField();
    Window loginWindow = new Window();
    FormLayout loginForm = new FormLayout();
    List<MessageObject> items = new ArrayList<MessageObject>();

    //buttons
    Button ui_bt_search = new Button();
    Button ui_previous = new Button();
    Button ui_next = new Button();
    Button ui_start = new Button();
    Button ui_end = new Button();
    Button UI_BT_public = new Button("Public");
    Button UI_BT_private = new Button("Private");
    Button UI_BT_submit = new Button("Login");
    Button ui_bt_down = new Button("");


    public Browser(Map<String, String[]> values) {
        queryParameters = values;
        ui_log.setStyleName("mono");
        ui_log.setId("logArea");
        ui_clusters.clear();
        ui_clusters.setItems(Config.CLUSTERNAMES);
        ui_clusters.addValueChangeListener(valueChangeEvent -> {
            ((Kafkabrowser) UI.getCurrent()).CLUSTER = valueChangeEvent.getValue();
            refreshTopics(true);
            fillPartitions();
        });

        if (queryParameters.get("cluster") != null) {
            ui_clusters.setValue(queryParameters.get("cluster")[0]);
        }else {
            ui_clusters.setValue(Config.CLUSTERNAMES.get(0).toString());
        }

        ui_log.setContentMode(ContentMode.HTML);
        ui_tb_search.setId("keyword");
        UI_prepare();
    }


    private void UI_prepare() {

        ui_log.setStyleName("mono");
        ui_log.setId("logArea");
        ui_log.setContentMode(ContentMode.PREFORMATTED);

        ui_limit.setItems(Shared.limits());
        ui_limit.setValue(((Kafkabrowser) UI.getCurrent()).LIMIT);
        ui_limit.setResponsive(true);
        ui_limit.setEmptySelectionAllowed(false);
        ui_limit.setWidth("60px");
        ui_parser.setWidth("70px");

        UL_loginForm();


        URI parameters = Page.getCurrent().getLocation();
        ui_parser.setItems(Shared.VIEWER.values());
        ui_parser.setValue(Shared.VIEWER.TEXT);
        ui_parser.setEmptySelectionAllowed(false);

        ui_bt_search.setStyleName("ownControl");
        ui_bt_search.setIcon(VaadinIcons.SEARCH);

        UI_BT_private.setStyleName("ownControl");
        UI_BT_private.setIcon(VaadinIcons.USER);


        ui_previous.setIcon(VaadinIcons.STEP_BACKWARD);
        ui_next.setIcon(VaadinIcons.STEP_FORWARD);

        ui_start.setIcon(VaadinIcons.ANGLE_DOUBLE_LEFT);
        ui_start.setCaption(Shared.START);

        ui_end.setIcon(VaadinIcons.ANGLE_DOUBLE_RIGHT);
        ui_end.setCaption(Shared.END);

        ui_clusters.setPageLength(0);
        ui_clusters.setEmptySelectionAllowed(false);


        ui_topics.setPageLength(0);
        ui_topics.setEmptySelectionAllowed(false);


        ui_partitions.setPageLength(0);
        ui_partitions.setEmptySelectionAllowed(false);
        ui_parser.addValueChangeListener(valueChangeEvent -> logRender());

        ui_log.setWidth("100%");
        ui_log.setHeight("250px");


        String[] parPartition = queryParameters.get("partition");
        if (parPartition != null) {
            this.partition = Integer.valueOf(parPartition[0]);
        }
        String[] parOffset = queryParameters.get("offset");
        if (parOffset != null) {
            setOffsetValue(parOffset[0]);
        } else {
            setOffsetValue(Shared.END);
        }


        refreshTopics(true);


        // grid
        grid.setFrozenColumnCount(1);
        grid.setFrozenColumnCount(2);
        grid.setFrozenColumnCount(3);
        grid.setFrozenColumnCount(4);
        grid.setColumns("partition", "offset", "key", "topic", "value");
        grid.getColumn("offset").setWidth(200);
        grid.getColumn("key").setWidth(200);
        grid.getColumn("topic").setHidden(true);
        grid.getColumn("partition").setHidden(true);

        grid.setWidth("100%");
        grid.setHeight("500px");
        grid.addItemClickListener(itemClick -> {
            String value = itemClick.getItem().getValue();
            lastLog = value;
            logRender();


        });
        grid.addSelectionListener(selectionEvent ->{
            try {
                String value = selectionEvent.getFirstSelectedItem().get().getValue();
                lastLog = value;
                logRender();
            } catch (Exception e) {

            }


        } );

        HeaderCell cell = filterRow.getCell("value");
        filterField.setWidth("100%");


        // Update filter When the filter input is changed
        filterField.addValueChangeListener(valueChangeEvent -> {
            grid.setItems(filterData(items, valueChangeEvent.getValue()));
        });
        cell.setComponent(filterField);


        ui_bt_down.addClickListener(clickEvent -> {
            String textoutput="";
            for (MessageObject message :items){
                textoutput+=message.getOffset()+" "+message.getValue()+"\r\n";
            }
            download(textoutput.getBytes(),ui_topics.getValue()+"",ui_partitions.getValue()+"");

        });


        controlArea.setWidth("98%");
        controlArea.setColumnExpandRatio(0, 0.01f);
        controlArea.setColumnExpandRatio(1, 0.01f);
        controlArea.setColumnExpandRatio(2, 0.90f);
        controlArea.setColumnExpandRatio(3, 0.01f);
        controlArea.setColumnExpandRatio(4, 0.02f);
        controlArea.setColumnExpandRatio(5, 0.03f);
        controlArea.setColumnExpandRatio(6, 0.01f);

        controlArea.addComponent(ui_start, 0, 0);
        controlArea.addComponent(ui_previous, 1, 0);
        controlArea.addComponent(slider, 2, 0);
        controlArea.addComponent(ui_next, 3, 0);
        controlArea.addComponent(ui_end, 4, 0);
        controlArea.addComponent(ui_tb_search, 5, 0);
        controlArea.addComponent(ui_limit, 6, 0);
        controlArea.addComponent(ui_parser, 7, 0);
        controlArea.addComponent(ui_bt_down, 8, 0);


        //place components
        hLayoutControl.addComponent(name);
        hLayoutControl.addComponent(ui_clusters);
        hLayoutControl.addComponent(ui_topics);
        hLayoutControl.addComponent(ui_partitions);
        hLayoutControl.addComponent(ui_offset);
        hLayoutControl.addComponent(ui_bt_search);
        hLayoutControl.addComponent(UI_BT_private);

        addComponent(hLayoutControl);
        addComponent(grid);
        addComponent(controlArea);
        addComponent(ui_log);

        //Listener's
        ui_limit.addValueChangeListener(valueChangeEvent -> {
            ((Kafkabrowser) UI.getCurrent()).LIMIT = valueChangeEvent.getValue();
            setOffsetValue(ui_offset.getValue());
            UI_refresh();
        });

        UI_BT_private.addClickListener(clickEvent -> {
            System.out.println(((Kafkabrowser) UI.getCurrent()).USERNAME);
            if (((Kafkabrowser) UI.getCurrent()).USERNAME.equals(Shared.ANONYMOUS)) {

                loginWindow.center();
                ((Kafkabrowser) UI.getCurrent()).addWindow(loginWindow);
            } else {
                ((Kafkabrowser) UI.getCurrent()).USERNAME = Shared.ANONYMOUS;
                refreshTopics(true);
                UI_BT_private.setCaption("Private");
            }
        });


        ui_previous.addClickListener(clickEvent -> {
            double step = slider.getValue() - ((Kafkabrowser) UI.getCurrent()).LIMIT;
            if(step<slider.getMin()){
                step=slider.getMin();
            }
            slider.setValue(step);
        });
        ui_next.addClickListener(clickEvent -> {
            double step = slider.getValue() + ((Kafkabrowser) UI.getCurrent()).LIMIT;
            if(step>slider.getMax()){
                step=slider.getMax();
            }
            slider.setValue(step);
        });
        ui_start.addClickListener(clickEvent -> {
            setOffsetValue(Shared.START);
            UI_refresh();
        });

        ui_end.addClickListener(clickEvent -> {
            setOffsetValue(Shared.END);
            UI_refresh();
        });

        ui_topics.addValueChangeListener(valueChangeEvent -> {
            grid.setItems();
            fillPartitions();
        });
        ui_partitions.addValueChangeListener(valueChangeEvent -> {
            if (init) {
                setOffsetValue(Shared.END);
            }

            UI_refresh();

        });

        ui_bt_search.addClickListener(

                clickEvent -> {
                    setOffsetValue(ui_offset.getValue());
                    UI_refresh();

                }
        );


        slider.addValueChangeListener(valueChangeEvent ->
                {
                    if (slider.getValue() == slider.getMin()) {
                        ui_previous.setEnabled(false);
                    } else {
                        ui_previous.setEnabled(true);
                    }
                    if (slider.getValue() == slider.getMax()) {
                        ui_next.setEnabled(false);
                    } else {
                        ui_next.setEnabled(true);
                    }
                    if (sliderListerner) {
                        setOffsetValue(Shared.doubleToString(valueChangeEvent.getValue()));
                        UI_refresh();

                    }

                }
        );

        ui_tb_search.addValueChangeListener(valueChangeEvent ->   //init search
                searchAndMark(valueChangeEvent.getValue()));

        //refresh UI

        if (ui_topics.getValue() != null && !ui_topics.getValue().equals("")) {
            fillPartitions();
            refreshSliderWidget();
            UI_refresh();
        } else {
            Notification.show("Please select topic", "", Notification.Type.WARNING_MESSAGE);

        }

    }

    /*
    Login form
     */
    private void UL_loginForm() {

        loginWindow.setContent(loginForm);
        loginWindow.setModal(true);
        ui_tb_search.setId("keyword");

        name.setStyleName("h1");
        ui_tb_search.setId("keyword");

        loginForm.setWidth("400px");

        TextField tf1 = new TextField("Username");
        tf1.setIcon(VaadinIcons.USER);
        tf1.setRequiredIndicatorVisible(true);
        loginForm.addComponent(tf1);

        PasswordField tf2 = new PasswordField("Password");
        tf2.setIcon(VaadinIcons.LOCK);
        loginForm.addComponent(tf2);



        UI_BT_submit.addClickListener(clickEvent -> {
            try {
                KafkaConnect.initInstance(tf1.getValue(), tf2.getValue());
                loginWindow.close();
                refreshTopics(true);
                UI_BT_private.setCaption("Logout");


            } catch (Shared.ExceptionUnauthorized e) {
                Notification.show("Authorisation failed", "", Notification.Type.WARNING_MESSAGE);
            }


        });

        ui_bt_down.setIcon(VaadinIcons.DOWNLOAD_ALT);


        loginForm.addComponent(UI_BT_submit);
    }

    //filterData
    private List<MessageObject> filterData(List<MessageObject> items, String filter) {
        List<MessageObject> newlist = new ArrayList<MessageObject>();

        for (MessageObject item : items) {

            if (item.getValue().contains(filter)) {
                newlist.add(item);

            }

        }
        if (filter.isEmpty()) {
            newlist = items;
        }
        return newlist;
    }

    /*
    Refresh topics
     */

    private void refreshTopics(boolean init) {
        String[] topics = new String[0];
        try {
            topics = ImplTopics.getTopics();
        } catch (Exception e) {
            e.printStackTrace();
            //UI.getCurrent().getPage().reload();
        }
        ui_topics.setItems(topics);
        ui_topics.setWidth("400px");
        if (topics.length > 0) {
            ui_topics.setValue(topics[0]);
        }
        if (queryParameters.get("topic") != null) {
            ui_topics.setValue(queryParameters.get("topic")[0]);
        }
        ((Kafkabrowser) UI.getCurrent()).linkURL.setResource(new ExternalResource("?cluster=" + ui_clusters.getValue()+"&topic=" + ui_topics.getValue() + "&partition=" + partition + "&offset=" + ui_offset.getValue()));

    }

    /*
    Render log
     */
    private void logRender() {
        Enum viewer = ui_parser.getValue();
        Renderer renderer = new Renderer(lastLog);

        String out = "";
        switch (ui_parser.getValue()) {
            case HEX:
                out = renderer.renderHex();
                break;
            case XML:
                out = renderer.renderXML();
                break;
            case JSON:
                out = renderer.renderJSON();
                break;
            default:
                // raw
                out = lastLog;

        }

        JavaScript.getCurrent().execute(
                "$(\"#logArea\").resizable({ disabled: true });$(\"#logArea\").resizable(\"destroy\");console.log(\"testik\");");

        JavaScript.getCurrent().execute(
                "$(\"#logArea\").text(\""+ JSONValue.escape(out)+"\")");
        ui_log.setValue(out);
        JavaScript.getCurrent().execute(
                "$(\"#logArea\").resizable();console.log(\"testik\");");


        searchAndMark(ui_tb_search.getValue());


    }

    /*
    Serach in log
     */
    private void searchAndMark(String keyword) {

        JavaScript.getCurrent().execute(
                "    var options = {};" +
                        "    $(\"#logArea\").unmark({" +
                        "      done: function() {" +
                        "        $(\"#logArea\").mark('" + keyword + "', options);" +
                        "      }" +
                        "    });");
    }

    /*
    Set offset value
     */

    private void setOffsetValue(String value) {

        ui_offset.setValue(value);//re-set offset


        //regenerate direct url link
        refreshDirectUrl();
    }


    /*
    Direct url
     */
    private void refreshDirectUrl() {
        ((Kafkabrowser) UI.getCurrent()).linkURL.setResource(new

                ExternalResource("?cluster=" + ui_clusters.getValue() +"&topic=" + ui_topics.getValue() + "&partition=" + ui_partitions.getValue() + "&offset=" + ui_offset.getValue()));


    }


    /*
    Fill partitions combobox
     */

    private void fillPartitions() {
        Integer[] topicObject = null;
        try {
            topicObject = ImplTopics.getTopicPartitions(ui_topics.getValue());

            ui_partitions.setItems(topicObject);
            ui_partitions.setValue(partition);
       } catch (Exception e) {
            e.printStackTrace();
        }


        init = true;


    }

    /*
    refresh UI
     */

    private void UI_refresh() {
        ui_log.setValue("");
        lastLog = "";
        List<MessageObject> messages = null;


        try {

            messages = ImplTopics.getMessages(ui_topics.getValue(), ui_partitions.getValue(), ui_offset.getValue() + "");

            refreshSliderWidget();

            if (messages.size() == 0) {
                ui_previous.setEnabled(false);
                ui_next.setEnabled(false);
                updateSliderPosition(slider.getValue());
            } else if (ui_offset.getValue().equals(Shared.END)) {
                updateSliderPosition(messages.get(messages.size() - 1).getDoubleOffset());
            } else if (ui_offset.getValue().equals(Shared.START)) {
                updateSliderPosition(messages.get(0).getDoubleOffset());
            } else {
                updateSliderPosition(Double.valueOf(ui_offset.getValue()));
            }

        } catch (Exception e) {
            noDataResponse();
        }
        items = messages;
        grid.setItems(messages);

    }

    /*
    NO data response
     */

    private void noDataResponse() {
        Notification.show("No data");
        ui_offset.setCaption("Offset of range (0-0)");
        slider.setMin(0);
        slider.setMax(0);
        slider.setValue((double) 0);

    }


    /*
    Update slider position. Disable listener
     */
    private void updateSliderPosition(Double offset) {
        sliderListerner = false;
        slider.setValue(offset);
        sliderListerner = true;
    }

    /*
    Refresh slider
     */
    private void refreshSliderWidget() {

        sliderListerner = false;
        InfoObject topicObject = null;
        try {
            topicObject = ImplTopics.getTopicPartition(ui_topics.getValue(), ui_partitions.getValue());
            slider.setWidth("100%");
            slider.setMin(Double.valueOf(topicObject.getStart()));
            slider.setMax(Double.valueOf(topicObject.getHighwater()) - 1);

            ui_offset.setCaption("Offset of range (" + Shared.doubleToString(slider.getMin()) + " - " +  Shared.doubleToString(slider.getMax()) + ")");
        } catch (Exception e) {
            Notification.show("Bad data", Notification.Type.WARNING_MESSAGE);
            e.printStackTrace();
        }


        sliderListerner = true;

    }

    public void download(byte[] toDownload,String topic,String partition) {
        StreamResource.StreamSource source = new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                return new ByteArrayInputStream(toDownload);
            }
        };
        // by default getStream always returns new DownloadStream. Which is weird because it makes setting stream parameters impossible.
        // It seems to be working before in earlier versions of Vaadin. We'll override it.
        String filename="kafkabrowser__"+topic+"__"+partition+".txt";
        StreamResource resource = new StreamResource(source,filename ) {
            DownloadStream downloadStream;
            @Override
            public synchronized DownloadStream getStream() {
                if (downloadStream==null)
                    downloadStream = super.getStream();
                return downloadStream;
            }
        };
        resource.getStream().setParameter("Content-Disposition","attachment;filename=\""+filename+"\""); // or else browser will try to open resource instead of download it
        resource.getStream().setParameter("Content-Type","application/octet-stream");
        resource.getStream().setCacheTime(0);
        ResourceReference ref = new ResourceReference(resource, this, "download");
        this.setResource("download", resource); // now it's available for download
        Page.getCurrent().open(ref.getURL(), null);
    }

}
