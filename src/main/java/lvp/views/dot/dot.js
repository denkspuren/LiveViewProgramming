class Dot {
    constructor(container, width, height) {
        this.container = container;
        this.width = width;
        this.height = height;
    }
    draw(dotString) {
        const parsedData = vis.parseDOTNetwork(dotString);

        const data = {
            nodes: parsedData.nodes,
            edges: parsedData.edges
        };
        const options = parsedData.options;
        options.width = this.width.toString();
        options.height = this.height.toString();

        new vis.Network(this.container, data, options);
    }
}