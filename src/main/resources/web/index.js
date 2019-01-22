$('#loading').hide();

function getSelected(name) {
    return $('#' + name).find('option:selected').text();
}

function fillMenus(databases) {
    var category = getSelected("Category");
    var databaseSelect = $('#Graph');
    databaseSelect.empty();
    databaseSelect.append("<option>Select</option>");
    databases[category].forEach(function (d) {
        databaseSelect.append('<option>' + d + '</option>');
    });
    databaseSelect.on('change', function () {
        var graph = getSelected("Graph");
        var cat = getSelected("Category");
        $('#loading').show();
        $.post(serverAddr + 'clusterids/' + cat + "--" + graph).done(function (recv_data) {
            var clusteridMenu = $('#ClusterId');
            let data = recv_data.clusterids;
            // data.forEach(function (d) {
            //     clusteridMenu.append('<option>' + d + '</option>');
            // });

            autocomplete(document.getElementById("ClusterId"), data);

            document.getElementById("NumOfV").innerHTML = recv_data.NumOfV;
            document.getElementById("NumOfE").innerHTML = recv_data.NumOfE;
            document.getElementById("NumOfC").innerHTML = data.length;
            $('#loading').hide();
        });
    });
}

function initializeGraphsMenu(databases) {
    console.log(databases);
    var categoriesSelect = $('#Category');
    Object.keys(databases).forEach(function (d) {
        categoriesSelect.append('<option>' + d + '</option>');
    });
    categoriesSelect.on('change', function () {
        fillMenus(databases);
    });
    fillMenus(databases);
}

var serverAddr = "http://localhost:2342/";


$(document).ready(function () {
    $.get(serverAddr + 'databases/')
        .done(initializeGraphsMenu)
        .fail(function (jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
        });

    $("#ShowVertexLabels").on('change', function () {
        var showVLabels = getSelected("ShowVertexLabels");
        if (showVLabels == 'no') cy.nodes().style('label', '');
    });

    $("#ShowEdgeLabels").on('change', function () {
        var showVLabels = getSelected("ShowEdgeLabels");
        if (showVLabels == 'no') cy.edges().style('label', '');
    });

});

function action() {
    var selectedAction = getSelected("Action");
    var cat = getSelected("Category");
    var graph = getSelected("Graph");
    var cid = document.getElementById("ClusterId").value;
    if (selectedAction == "Show Cluster and its Neighbors") {
        $('#loading').show();
        $.post(serverAddr + 'clusterandns/' + cat + "--" + graph + "--" + cid).done(function (data) {
            drawGraph(data, function () {
                cy.fit(cy.elements(), 40)
            });
            $('#loading').hide();
        });
    }

    if (selectedAction == "Show Cluster") {
        $('#loading').show();
        $.post(serverAddr + 'cluster/' + cat + "--" + graph + "--" + cid).done(function (data) {
            drawGraph(data, function () {
                cy.fit(cy.elements(), 40)
            });
            $('#loading').hide();
        });
    }

    if (selectedAction == "Show Cluster Incremental") {
        $('#loading').show();
        $.post(serverAddr + 'cluster_incremental/' + cat + "--" + graph + "--" + cid).done(function (data) {

        });

    }

    if (selectedAction == "Animate Incremental") {
        $('#loading').show();
        $.post(serverAddr + 'animation/' + cat + "--" + graph + "--" + cid).done(function (data) {

        });
    }
}





