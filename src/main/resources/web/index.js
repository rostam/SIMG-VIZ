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
        $('#loading').show();
        $.post(serverAddr + 'clusterids/' + category + "--" + graph).done(function (data) {
            var clusteridMenu = $('#ClusterId');
            data.forEach(function (d) {
                clusteridMenu.append('<option>' + d + '</option>');
            });
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
var fade = false;
var selectedNode = null;


$(document).ready(function () {
    $.get(serverAddr + 'databases/')
        .done(initializeGraphsMenu)
        .fail(function (jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
        });
    document.getElementById("go").onclick = function () {
        var cat = getSelected("Category");
        var graph = getSelected("Graph");
        var cid = getSelected("ClusterId");
        $('#loading').show();
        $.post(serverAddr + 'clusterandns/' + cat + "--" + graph + "--" + cid).done(function (data) {
            drawGraph(data, function () {
                cy.fit(cy.elements(), 40)
            });
            $('#loading').hide();
        });
    };

    document.getElementById("go-only-cluster").onclick = function () {
        var cat = getSelected("Category");
        var graph = getSelected("Graph");
        var cid = getSelected("ClusterId");
        $('#loading').show();
        $.post(serverAddr + 'cluster/' + cat + "--" + graph + "--" + cid).done(function (data) {
            drawGraph(data, function () {
                cy.fit(cy.elements(), 40)
            });
            $('#loading').hide();
        });
    };

});





