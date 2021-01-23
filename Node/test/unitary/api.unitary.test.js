const supertest = require('supertest');
const app = require('../../src/app');
const AWS = require('aws-sdk');

const request = supertest(app);

const mockedData = {
    Items: [
        {
            id: 0,
            title: 'Inglorious Basterds',
            year: 2009,
            director: 'Quentin Tarantino'
        },
        {
            id: 1,
            title: 'GoodFellas',
            year: 1990,
            director: 'Martin Scorsese'
        }
    ]
}

const getMockedMovie = (number) => mockedData.Items[number - 1];

describe('Unit tests', () => {

    beforeAll(() => {

        const put = (params, cb)=> {
            const error = Object.keys(params.Item).length <= 1 ? new Error('Error at mocked AWS module') : null;

            cb(error, params.Item);
        };

        const scan = (params, cb)=> {
            cb(null, mockedData);
        };

        AWS.DynamoDB.DocumentClient = jest.fn().mockReturnValue({ put, scan });
    })

    test('get all films', async () => {

        const response = await request.get('/api/films').expect(200);
        const [{title: title1}, {title: title2}] = response.body;

        expect(response.statusCode).toBe(200);
        expect(title1).toBe(getMockedMovie(1).title);
        expect(title2).toBe(getMockedMovie(2).title);


    });

    test('Create new film', async () => {
        const film = { title: 'Scarface', year : 1983, director: 'Brian de Palma'};
        const response = await request.post('/api/films').send(film).expect(201);
        const { title, year, director, id} = response.body;

        expect(id).toBe(0);
        expect(title).toBe(film.title);
        expect(year).toBe(film.year);
        expect(director).toBe(film.director);
        expect(response.statusCode).toBe(201);
    });

    test('Create new film fails when a correct film is not provided', async () => {

        const response = await request.post('/api/films').send(null).expect(400);

        expect(response.statusCode).toBe(400);
        expect(response.error).not.toBeNull();
    });

})
