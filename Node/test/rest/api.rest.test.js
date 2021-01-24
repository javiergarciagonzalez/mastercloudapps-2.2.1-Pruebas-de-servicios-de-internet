const supertest = require('supertest');
const { GenericContainer } = require("testcontainers");
const app = require('../../src/app');
const AWS = require('aws-sdk');
const createTableIfNotExist = require('../../src/db/createTable');

let dynamoContainer;

const populateDB = async (request) => {
    const film = { title: 'Watchmen', year : 2009, director: 'Zack Snyder'};
    await request.post('/api/films').send(film).expect(201);
}

describe('Test container tests', () => {

    beforeAll(async () => {
        dynamoContainer = await new GenericContainer('amazon/dynamodb-local','1.13.6')
            .withExposedPorts(8000)
            .start()
            .catch((err)=>{console.log(err)});

        const dynamoPort = dynamoContainer.getMappedPort(8000);

        AWS.config.update({
            region: process.env.AWS_REGION || 'local',
            endpoint: process.env.AWS_DYNAMO_ENDPOINT || `http://localhost:${dynamoPort}`,
            accessKeyId: "xxxxxx", // No es necesario poner nada aquí
            secretAccessKey: "xxxxxx" // No es necesario poner nada aquí
        });

        request = supertest(app);
        await createTableIfNotExist('films');
        await populateDB(request);
    });

    afterAll(async () => {
        await dynamoContainer.stop();
    });

    test('get all films', async () => {
        const response = await request.get('/api/films').expect(200);
        const [{title, id}] = response.body;

        expect(response.statusCode).toBe(200);
        expect(id).toBe(0);
        expect(title).toBe('Watchmen');
    });

    test('Create a new film', async () => {
        const film = { title: 'Scarface', year : 1983, director: 'Brian de Palma'};
        const response = await request.post('/api/films').send(film).expect(201);
        const { title, year, director, id} = response.body;

        expect(id).toBe(1); // DB preloaded with one film by default
        expect(title).toBe(film.title);
        expect(year).toBe(film.year);
        expect(director).toBe(film.director);
        expect(response.statusCode).toBe(201);
    });
});
