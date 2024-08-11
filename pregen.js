const fs = require('fs');
const path = require('path');

const FOLDER = "./src/main/resources";
const IDS = [
    "minecraft:stone_bricks",
    "minecraft:mossy_stone_bricks",
    "minecraft:deepslate_bricks",
    "minecraft:bricks",
    "minecraft:mud_bricks",
    "minecraft:prismarine_bricks",
    "minecraft:nether_bricks",
    "minecraft:red_nether_bricks",
    "minecraft:polished_blackstone_bricks",
    "minecraft:end_stone_bricks",
    "minecraft:quartz_bricks"
];

const blockTags = {
    "mineable/pickaxe": [],
    "needs_stone_tool": []
};

const lang = [];

IDS.forEach(id => {
    const namespace = id.split(':')[0];
    const path = id.split(':')[1];

    lang.push(path);

    //Get all block tags
    for (let key of Object.keys(blockTags)) {
        blockTags[key].push(id);
    }

    //Write blockstates
    writeJsonToFile(`${FOLDER}/assets/waterways/blockstates/aqueduct/${path}.json`, genBlockstate(path));

    //Write block models
    let blockModels = genBlockModels(namespace, path);
    for (let key of Object.keys(blockModels)) {
        writeJsonToFile(`${FOLDER}/assets/waterways/models/block/aqueduct/${path}/${key}.json`, blockModels[key]);
    }

    //Write item models
    writeJsonToFile(`${FOLDER}/assets/waterways/models/item/aqueduct/${path}.json`, genItemModels(path));

    //Write loot tables
    writeJsonToFile(`${FOLDER}/data/waterways/loot_tables/blocks/aqueduct/${path}.json`, genLootTables(path));

    //Write recipes
    writeJsonToFile(`${FOLDER}/data/waterways/recipes/aqueduct/${path}.json`, genRecipes(id, path));
});

//Write default models
const defaultModels = genDefaultModels();
for (let key of Object.keys(defaultModels)) {
    writeJsonToFile(`${FOLDER}/assets/waterways/models/block/aqueduct/${key}.json`, defaultModels[key]);
}

//Write block tags
for (let key of Object.keys(blockTags)) {
    writeJsonToFile(`${FOLDER}/data/minecraft/tags/blocks/${key}.json`, genBlockTags(blockTags[key]));
}

//Write lang
writeJsonToFile(`${FOLDER}/assets/waterways/lang/en_us.json`, genLang(lang));

function genBlockstate(path) {
    return {
        "multipart": [
            {
                "apply": {
                    "model": `waterways:block/aqueduct/${path}/base`
                }
            },
            {
                "when": {
                    "north": "false"
                },
                "apply": {
                    "model": `waterways:block/aqueduct/${path}/north`
                }
            },
            {
                "when": {
                    "east": "false"
                },
                "apply": {
                    "model": `waterways:block/aqueduct/${path}/east`
                }
            },
            {
                "when": {
                    "south": "false"
                },
                "apply": {
                    "model": `waterways:block/aqueduct/${path}/south`
                }
            },
            {
                "when": {
                    "west": "false"
                },
                "apply": {
                    "model": `waterways:block/aqueduct/${path}/west`
                }
            }
        ]
    };
}

function genDefaultModels() {
    return {
        base: {
            "parent": "block/block",
            "elements": [
                {
                    "from": [0, 0, 0],
                    "to": [16, 10, 16],
                    "faces": {
                        "north": { "uv": [0, 6, 16, 16], "texture": "#texture" },
                        "east": { "uv": [0, 6, 16, 16], "texture": "#texture" },
                        "south": { "uv": [0, 6, 16, 16], "texture": "#texture" },
                        "west": { "uv": [0, 6, 16, 16], "texture": "#texture" },
                        "up": { "uv": [0, 0, 16, 16], "texture": "#texture" },
                        "down": { "uv": [0, 0, 16, 16], "texture": "#texture" }
                    }
                },
                {
                    "from": [0, 10, 0],
                    "to": [4, 16, 4],
                    "faces": {
                        "north": { "uv": [12, 0, 16, 6], "texture": "#texture" },
                        "east": { "uv": [12, 0, 16, 6], "texture": "#texture" },
                        "south": { "uv": [0, 0, 4, 6], "texture": "#texture" },
                        "west": { "uv": [0, 0, 4, 6], "texture": "#texture" },
                        "up": { "uv": [0, 0, 4, 4], "texture": "#texture" }
                    }
                },
                {
                    "from": [12, 10, 0],
                    "to": [16, 16, 4],
                    "faces": {
                        "north": { "uv": [0, 0, 4, 6], "texture": "#texture" },
                        "east": { "uv": [12, 0, 16, 6], "texture": "#texture" },
                        "south": { "uv": [12, 0, 16, 6], "texture": "#texture" },
                        "west": { "uv": [0, 0, 4, 6], "texture": "#texture" },
                        "up": { "uv": [12, 0, 16, 4], "texture": "#texture" }
                    }
                },
                {
                    "from": [0, 10, 12],
                    "to": [4, 16, 16],
                    "faces": {
                        "north": { "uv": [12, 0, 16, 6], "texture": "#texture" },
                        "east": { "uv": [0, 0, 4, 6], "texture": "#texture" },
                        "south": { "uv": [0, 0, 4, 6], "texture": "#texture" },
                        "west": { "uv": [12, 0, 16, 6], "texture": "#texture" },
                        "up": { "uv": [0, 12, 4, 16], "texture": "#texture" }
                    }
                },
                {
                    "from": [12, 10, 12],
                    "to": [16, 16, 16],
                    "faces": {
                        "north": { "uv": [0, 0, 4, 6], "texture": "#texture" },
                        "east": { "uv": [0, 0, 4, 6], "texture": "#texture" },
                        "south": { "uv": [12, 0, 16, 6], "texture": "#texture" },
                        "west": { "uv": [12, 0, 16, 6], "texture": "#texture" },
                        "up": { "uv": [12, 12, 16, 16], "texture": "#texture" }
                    }
                }
            ]
        },
        east: {
            "parent": "block/block",
            "elements": [
                {
                    "from": [12, 10, 4],
                    "to": [16, 16, 12],
                    "faces": {
                        "east": { "uv": [4, 0, 12, 6], "texture": "#texture" },
                        "west": { "uv": [4, 0, 12, 6], "texture": "#texture" },
                        "up": { "uv": [12, 4, 16, 12], "texture": "#texture" }
                    }
                }
            ]
        },
        north: {
            "parent": "block/block",
            "elements": [
                {
                    "from": [4, 10, 0],
                    "to": [12, 16, 4],
                    "faces": {
                        "north": { "uv": [4, 0, 12, 6], "texture": "#texture" },
                        "south": { "uv": [4, 0, 12, 6], "texture": "#texture" },
                        "up": { "uv": [4, 0, 12, 4], "texture": "#texture" }
                    }
                }
            ]
        },
        south: {
            "parent": "block/block",
            "elements": [
                {
                    "from": [4, 10, 12],
                    "to": [12, 16, 16],
                    "faces": {
                        "north": { "uv": [4, 0, 12, 6], "texture": "#texture" },
                        "south": { "uv": [4, 0, 12, 6], "texture": "#texture" },
                        "up": { "uv": [4, 12, 12, 16], "texture": "#texture" }
                    }
                }
            ]
        },
        west: {
            "parent": "block/block",
            "elements": [
                {
                    "from": [0, 10, 4],
                    "to": [4, 16, 12],
                    "faces": {
                        "east": { "uv": [4, 0, 12, 6], "texture": "#texture" },
                        "west": { "uv": [4, 0, 12, 6], "texture": "#texture" },
                        "up": { "uv": [0, 4, 4, 12], "texture": "#texture" }
                    }
                }
            ]
        }
    }
}

function genBlockModels(namespace, path) {
    const directions = [
        "east",
        "north",
        "south",
        "west"
    ];

    const models = {
        base: {
            "parent": "waterways:block/aqueduct/base",
            "textures": {
                "texture": `${namespace}:block/${path}`,
                "particle": `${namespace}:block/${path}`
            }
        }
    };

    directions.forEach(dir => {
        models[dir] = {
            "parent": `waterways:block/aqueduct/${dir}`,
            "textures": {
                "texture": `${namespace}:block/${path}`,
                "particle": `${namespace}:block/${path}`
            }
        }
    });

    return models;
}

function genItemModels(path) {
    return {
        "parent": `waterways:block/aqueduct/${path}/base`
    }
}

function genBlockTags(blocks) {
    return {
        "replace": false,
        "values": blocks
    }
}

function genLootTables(path) {
    return {
        "type": "minecraft:block",
        "pools": [
            {
                "name": "loot_pool",
                "rolls": 1,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": `waterways:aqueduct/${path}`
                    }
                ],
                "conditions": [
                    {
                        "condition": "minecraft:survives_explosion"
                    }
                ]
            }
        ]
    }
}

function genRecipes(id, path) {
    return {
        "type": "minecraft:crafting_shaped",
        "pattern": [
            "X X",
            "MXM"
        ],
        "key": {
            "X": {
                "item": id
            },
            "M": {
                "item": "minecraft:clay_ball",
                "count": 8
            }
        },
        "result": {
            "item": `waterways:aqueduct/${path}`
        }
    }
}

function genLang(entries) {
    const lang = {
        "itemgroup.waterways.creativetab": "Waterways"
    }

    entries.forEach(entry => {
        const parts = entry.split("_");
        parts.forEach((_, i) => {
            parts[i] = capitalize(parts[i]);
        });
        parts.push("Aqueduct");

        lang["block.waterways.aqueduct." + entry] = parts.join(" ");
    });

    return lang;
}

function capitalize(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

function writeJsonToFile(relativeFilePath, data) {
    const absolutePath = path.resolve(__dirname, relativeFilePath);
    const dir = path.dirname(absolutePath);

    fs.mkdirSync(dir, { recursive: true });

    fs.writeFileSync(absolutePath, JSON.stringify(data, null, 4), 'utf8');

    console.log(`Generated: ${absolutePath}`);
}