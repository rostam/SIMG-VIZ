/**
 * Add a custom Qtip to the vertices and edges of the graph.
 */
function addQtip() {
    cy.elements().qtip({
        content: function () {
            var qtipText = '';
            for (var key in this.data()) {
                if (key != 'properties' && key != 'pie_parameters') {
                    qtipText += key + ' : ' + this.data(key) + '<br>';

                }
            }
            var properties = this.data('properties');
            for (var property in properties) {
                if (properties.hasOwnProperty(property)) {
                    qtipText += property + ' : ' + properties[property] + '<br>';
                }
            }
            return qtipText;
        },
        position: {
            my: 'top center',
            at: 'bottom center'
        }
    });
}


function fadeUnselected() {
    // if (fade == false) fade = true;
    // else fade = false;
    // if (fade) {
    cy.elements().unselectify();
    /* if a vertex is selected, fade all edges and vertices
     that are not in direct neighborhood of the vertex */
    cy.on('cxttap', 'node', function (e) {
        var node = e.cyTarget;
        selectedNode = node;
        var neighborhood = node.neighborhood().add(node);

        cy.elements().addClass('faded');
        neighborhood.removeClass('faded');
    });
    // remove fading by clicking somewhere else
    cy.on('cxttap', function (e) {
        if (e.cyTarget === cy) {
            cy.elements().removeClass('faded');
        }
    });
    // } else {
    //     cy.elements().unselectify();
    //     cy.on('cxttap', 'node', function (e) {
    //     });
    //     // remove fading by clicking somewhere else
    //     cy.on('cxttap', function (e) {
    //     });
    // }
}


function buildCytoscape() {
    return cytoscape({
        container: document.getElementById('canvas'),
        // these options hide parts of the graph during interaction
        //hideEdgesOnViewport: true,
        //hideLabelsOnViewport: true,

        // this is an alternative that uses a bitmap during interaction
        //textureOnViewport: true,

        // interpolate on high density displays instead of increasing resolution
        //pixelRatio: 1,

        // a motion blur effect that increases perceived performance for little or no cost
        // motionBlur: true,
        style: cytoscape.stylesheet()
            .selector('node')
            .css({
                // define label content and font
                'content': function (node) {
                    if ($('#showVertexLabels').is(':checked')) {
                        return node.data('label');
                    }

                    if (node.data('properties')['vis_label'] != null) {
                        return node.data('properties')['vis_label'];
                    }

                    // var labelString = getLabel(node, vertexLabelKey, useDefaultLabel);
                    //
                    // var properties = node.data('properties');
                    //
                    // if (properties['count'] != null) {
                    //     labelString += ' (' + properties['count'] + ')';
                    // }
                    return "";
                },
                // if the count shall effect the vertex size, set font size accordingly
                'font-size': function (node) {
                    return 14;
                },
                'text-valign': 'center',
                'color': 'black',
                'background-color': function (node) {
                    var col = node.data('properties')['color'];
                    if (col.indexOf(",") == -1) return col;
                    else {
                        return col.substring(0, col.indexOf(","));
                    }
                },
                'border-width': function (node) {
                    return 3;
                },

                /* size of vertices can be determined by property count
                 count specifies that the vertex stands for
                 1 or more other vertices */
                'width': function (node) {
                    return '60px';
                },
                'height': function (node) {
                    return '60px'
                },
                'text-wrap': 'wrap'
            })
            .selector('edge')
            .css({
                'opacity': 0.9,
                //"edge-text-rotation": "autorotate",
                'curve-style': 'bezier',
                'text-opacity': 1.0,
                'font-weight': 'normal',
                // layout of edge and edge label
                'content': function (edge) {
                    if ($('#showEdgeLabels').is(':checked')) {
                        return edge.data('label');
                    }

                    if (edge.data('properties')['vis_label'] != null) {
                        return edge.data('properties')['vis_label'];
                    }

                    // var labelString = getLabel(edge, edgeLabelKey, useDefaultLabel);
                    //
                    // if(labelString == 'undefined') {
                    //     return '';
                    // }

                    // var properties = edge.data('properties');
                    //
                    // if (properties['count'] != null) {
                    //     labelString += ' (' + properties['count'] + ')';
                    // }

                    return "";
                },
                // if the count shall effect the vertex size, set font size accordingly
                'font-size': function (node) {
                    if ($('#showCountAsSize').is(':checked')) {
                        var count = node.data('properties')['count'];
                        if (count != null) {
                            count = count / maxVertexCount;
                            // surface of vertices is proportional to count
                            return Math.max(2, Math.sqrt(count * 10000 / Math.PI));
                        }
                    }
                    return 14;
                },
                'line-color': function (edge) {
                    if (edge.data('properties')['IsCorrect'] != null) {
                        if (edge.data('properties')['IsMissing'] == "true") {
                            return 'black';
                        } else if (edge.data('properties')['IsCorrect'] == "true") {
                            return 'forestgreen';
                        } else {
                            return 'red';
                        }
                    }
                    return '#999';
                },
                "text-outline-width": 0.3,
                "text-outline-color": "gray",
                //'#999',
                // width of edges can be determined by property count
                // count specifies that the edge represents 1 or more other edges
                'width': function (edge) {
                    if ($('#showCountAsSize').is(':checked')) {
                        var count = edge.data('properties')['count'];
                        if (count != null) {
                            count = count / maxEdgeCount;
                            return Math.sqrt(count * 1000);
                        }
                    }
                    if (edge.data('properties')['Style'] == 'bold') {
                        return 6;
                    }

                    return 2;
                },
                'line-style': function (edge) {
                    if (edge.data('properties')['Style'] == undefined) {
                        return 'solid';
                    } else {
                        if (edge.data('properties')['Style'] == "bold")
                            return 'solid';
                        else return edge.data('properties')['Style'];
                    }
                },
                'target-arrow-shape': 'triangle',
                'target-arrow-color': '#000'
            })
            // properties of edges and vertices in special states, e.g. invisible or faded
            .selector('.faded')
            .css({
                'opacity': 0.25,
                'text-opacity': 0
            })
            .selector('.invisible')
            .css({
                'opacity': 0,
                'text-opacity': 0
            })
            .selector(':parent')
            .css({
                'background-opacity': 0.333
            })
        ,
        ready: function () {
            window.cy = this;
            cy.on('tap', 'node', function (e) {
                var node = e.cyTarget;
                selectedNode = node;
            });
            fadeUnselected();
        }
    });
}

/**
 * function called when the server returns the data
 * @param data graph data
 */
function drawGraph(data, finish) {

    // buffer the data to speed up redrawing
    bufferedData = data;

    // lists of vertices and edges
    var nodes = data.nodes;
    var edges = data.edges;
    //
    // if(nodes.length>2000) {
    //     var options = {
    //         name: 'preset',
    //         positions: undefined, // map of (node id) => (position obj); or function(node){ return somPos; }
    //         zoom: undefined, // the zoom level to set (prob want fit = false if set)
    //         pan: undefined, // the pan level to set (prob want fit = false if set)
    //         fit: true, // whether to fit to viewport
    //         padding: 30, // padding on fit
    //         animate: false, // whether to transition the node positions
    //         animationDuration: 500, // duration of animation in ms if enabled
    //         animationEasing: undefined, // easing of animation if enabled
    //         ready: undefined, // callback on layoutready
    //         stop: undefined // callback on layoutstop
    //     };
    //     cy.layout(options).run();
    //     return''
    // }

    // set conaining all distinct labels (property key specified by vertexLabelKey)
    var labels = new Set();

    // compute maximum count of all vertices, used for scaling the vertex sizes
    maxVertexCount = 0;
    // for (var i = 0; i < nodes.length; i++) {
    //     var node = nodes[i];
    //     var vertexCount = Number(node['data']['properties']['count']);
    //     if ((vertexCount != null) && (vertexCount > maxVertexCount)) {
    //         maxVertexCount = vertexCount;
    //     }
    //     if (getColorKeyForVertices() != 'label') {
    //         labels.add(node['data']['properties'][getColorKeyForVertices()]+"")
    //     } else {
    //         labels.add(node['data']['label']);
    //     }
    // }

    // generate random colors for the vertex labels
    // generateRandomColors(labels);

    // compute maximum count of all edges, used for scaling the edge sizes
    maxEdgeCount = 0;
    for (var j = 0; j < edges.length; j++) {
        var edge = edges[j];
        var edgeCount = Number(edge['data']['properties']['count']);
        if ((edgeCount != null) && (edgeCount > maxEdgeCount)) {
            maxEdgeCount = edgeCount;
        }
    }

    // update vertex and edge count after sampling
    // document.getElementById("svcount").innerHTML = nodes.length;
    // document.getElementById("secount").innerHTML = edges.length;

    cy.elements().remove();
    cy.add(nodes);
    cy.add(edges);

    if ($('#hideNullGroups').is(':checked')) {
        hideNullGroups();
    }

    if ($('#hideDisconnected').is(':checked')) {
        hideDisconnected();
    }

    if ($('#hideEdges').is(':checked')) {
        hideEdges();
    }
    addQtip();
    cy.layout(chooseLayout());

    if (cy.nodes().length != 0) {
        if (cy.nodes()[0].data("properties")["ClusterId"] != null) {
            var clusteridsselect = $('#clusterids');
            var uniqueClusterIds = {};
            cy.nodes().forEach(function (n) {
                var colors = n.data("properties")["color"];
                if (colors.indexOf(",") != -1) {
                    var styles = {};
                    var colorsSplitted = colors.split(",");
                    var size = 100 / colorsSplitted.length;
                    for (var i = 0; i < colorsSplitted.length; i++) {
                        styles["pie-" + (i + 1) + "-background-color"] = colorsSplitted[i];
                        styles["pie-" + (i + 1) + "-background-size"] = "" + size;
                    }
                    n.style(styles);
                }
                uniqueClusterIds[n.data('properties').ClusterId] = 0;
            });
            Object.keys(uniqueClusterIds).forEach(function (t) {
                clusteridsselect.append("<option>" + t + "</option>");
            });
        }
    }
    changed = false;
    // hide the loading gif
    finish();
}

function chooseLayout() {
// options for the force layout
    var preset = {
        name: 'preset',

        // called on `layoutready`
        ready: function () {
            // cy.edges().forEach(function (e) {
            //     e.style("opacity", getEdgeOpacity());
            // });
        },

        // called on `layoutstop`
        stop: function () {
        },

        // whether to animate while running the layout
        animate: false,

        // number of iterations between consecutive screen positions update (0 ->
        // only updated on the end)
        refresh: 4,

        // whether to fit the network view after when done
        fit: true,

        // padding on fit
        padding: 100,

        // constrain layout bounds; { x1, y1, x2, y2 } or { x1, y1, w, h }
        boundingBox: undefined,

        // whether to randomize node positions on the beginning
        randomize: false,

        // whether to use the JS console to print debug messages
        debug: false,

        // node repulsion (non overlapping) multiplier
        nodeRepulsion: 8000000,

        // node repulsion (overlapping) multiplier
        nodeOverlap: 10,

        // ideal edge (non nested) length
        idealEdgeLength: 1,

        // divisor to compute edge forces
        edgeElasticity: 100,

        // nesting factor (multiplier) to compute ideal edge length for nested edges
        nestingFactor: 5,

        // gravity force (constant)
        gravity: 250,

        // maximum number of iterations to perform
        numIter: 100,

        // initial temperature (maximum node displacement)
        initialTemp: 200,

        // cooling factor (how the temperature is reduced between consecutive iterations
        coolingFactor: 0.95,

        // lower temperature threshold (below this point the layout will end)
        minTemp: 1.0
    };

    var cose = {
        name: 'cose',

        // called on `layoutready`
        ready: function () {
        },

        // called on `layoutstop`
        stop: function () {
        },

        // whether to animate while running the layout
        animate: false,

        // number of iterations between consecutive screen positions update (0 ->
        // only updated on the end)
        refresh: 4,

        // whether to fit the network view after when done
        fit: true,

        // padding on fit
        padding: 30,

        // constrain layout bounds; { x1, y1, x2, y2 } or { x1, y1, w, h }
        boundingBox: undefined,

        // whether to randomize node positions on the beginning
        randomize: true,

        // whether to use the JS console to print debug messages
        debug: false,

        // node repulsion (non overlapping) multiplier
        nodeRepulsion: 8000000,

        // node repulsion (overlapping) multiplier
        nodeOverlap: 10,

        // ideal edge (non nested) length
        idealEdgeLength: 1,

        // divisor to compute edge forces
        edgeElasticity: 100,

        // nesting factor (multiplier) to compute ideal edge length for nested edges
        nestingFactor: 5,

        // gravity force (constant)
        gravity: 10,

        // maximum number of iterations to perform
        numIter: 200,

        // initial temperature (maximum node displacement)
        initialTemp: 200,

        // cooling factor (how the temperature is reduced between consecutive iterations
        coolingFactor: 0.95,

        // lower temperature threshold (below this point the layout will end)
        minTemp: 1.0
    };

    var random = {
        name: 'random',
        fit: false, // whether to fit to viewport
        padding: 30, // fit padding
        boundingBox: {x1: 0, y1: 0, w: 5000, h: 5000}, // constrain layout bounds; { x1, y1, x2, y2 }
        // or { x1, y1, w, h }
        animate: false, // whether to transition the node positions
        animationDuration: 0, // duration of animation in ms if enabled
        animationEasing: undefined, // easing of animation if enabled
        ready: undefined, // callback on layoutready
        stop: undefined // callback on layoutstop
    };

    var radialRandom = {
        name: 'preset',
        positions: function (node) {
            var r = Math.random() * 1000001;
            var theta = Math.random() * 2 * (Math.PI);
            return {
                x: Math.sqrt(r) * Math.sin(theta),
                y: Math.sqrt(r) * Math.cos(theta)
            };
        },
        zoom: undefined,
        pan: undefined,
        fit: true,
        padding: 30,
        animate: false,
        animationDuration: 500,
        animationEasing: undefined,
        ready: undefined,
        stop: undefined
    };

    // if (getSelectedLayout().toString().indexOf("on server") != -1) {
    return preset;
    // } else if (getSelectedLayout().toString() == "FR layout") {
    //     return cose;
    // } else {
    //     return radialRandom;
    // }
}

cy = buildCytoscape();

var fade = false;
var selectedNode = null;
function fadeUnselected() {
    // if (fade == false) fade = true;
    // else fade = false;
    // if (fade) {
    cy.elements().unselectify();
    /* if a vertex is selected, fade all edges and vertices
     that are not in direct neighborhood of the vertex */
    cy.on('cxttap', 'node', function (e) {
        var node = e.cyTarget;
        selectedNode = node;
        var neighborhood = node.neighborhood().add(node);

        cy.elements().addClass('faded');
        neighborhood.removeClass('faded');
    });
    // remove fading by clicking somewhere else
    cy.on('cxttap', function (e) {

        if (e.cyTarget === cy) {
            cy.elements().removeClass('faded');
        }
    });
    // } else {
    //     cy.elements().unselectify();
    //     cy.on('cxttap', 'node', function (e) {
    //     });
    //     // remove fading by clicking somewhere else
    //     cy.on('cxttap', function (e) {
    //     });
    // }
}
