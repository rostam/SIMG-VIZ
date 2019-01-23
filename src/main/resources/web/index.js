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

    $("#ColorEdgesOptions").on('change', function () {
        var showVLabels = getSelected("ColorEdgesOptions");
        if (showVLabels == 'Cluster value') {
            cy.edges().forEach(function (edge) {
                if (edge.data('properties')['IsCorrect'] != null) {
                    if (edge.data('properties')['IsMissing'] == "true") {
                        edge.style('line-color', 'black');
                        return;
                    } else if (edge.data('properties')['IsCorrect'] == "true") {
                        edge.style('line-color', 'forestgreen');
                        return;
                    } else {
                        edge.style('line-color', 'forestgreen');
                        return;
                    }
                }
                edge.style('line-color', '#999');
                return;
            });
        } else if (showVLabels == 'Incremental') {
            cy.edges().forEach(function (e) {
                e.style("line-color",e.data().properties.ColorIncremental);
            });
        }
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
                cy.fit(cy.elements(), 40);
            });
            $('#loading').hide();
        });
    }

    if (selectedAction == "Show Cluster") {
        $('#loading').show();
        $.post(serverAddr + 'cluster/' + cat + "--" + graph + "--" + cid).done(function (data) {
            drawGraph(data, function () {
                cy.fit(cy.elements(), 40);
            });
            $('#loading').hide();
        });
    }

    if (selectedAction == "Show Cluster Incremental") {
        $('#loading').show();
        $.post(serverAddr + 'cluster_incremental/' + cat + "--" + graph + "--" + cid).done(function (data) {
            drawGraph(data, function () {
                cy.fit(cy.elements(), 40);
            });
            $('#loading').hide();
        });

    }

    if (selectedAction == "Show Full Incremental") {
        $('#loading').show();
        $.post(serverAddr + 'full/' + cat + "--" + graph + "--" + cid).done(function (data) {
            cy2 = cytoscape({
                container: document.getElementById('canvas'),
                style: cytoscape.stylesheet()
                    .selector('node')
                    .css({
                        'background-color': function (node) {
                            var col = node.data('properties')['color'];
                            if (col.indexOf(",") == -1) return col;
                            else {
                                return col.substring(0, col.indexOf(","));
                            }
                        },
                        'width' : '8px',
                        'height' : '8px'
                    })
                    .selector('edge')
                    .css({
                        'opacity': 0.5,
                        'line-color': function (e) {
                            return e.data().properties.ColorIncremental;
                        },
                        'width' : '1px'

                    })
            });
            var nodes = data.nodes;
            var edges = data.edges;
            cy.elements().remove();
            cy2.elements().remove();
            cy2.add(nodes);
            cy2.add(edges);
            cy2.layout({name:"preset"});
            cy2.fit(cy2.elements(), 5);
            // drawGraph(data, function () {
            //     cy.nodes().style("width","10px");
            //     cy.nodes().style("height","10px");
            //     cy.fit(cy.elements(), 40);
            // });
            $('#loading').hide();
        });
    }
}





