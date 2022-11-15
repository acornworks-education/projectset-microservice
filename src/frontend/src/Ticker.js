import { useEffect, useState } from 'react';
import {Container, Row, Col, Table, Button, Form} from 'react-bootstrap';


function Ticker() {
    const [ tickers, setTickers ] = useState([]);
    const [ loaded, setLoaded ] = useState(false);
    const [ ticker, setTicker ] = useState('');
    const [ name, setName ] = useState('');
    const urlPrefix = !!process.env.REACT_APP_ENDPOINT ? process.env.REACT_APP_ENDPOINT : 'http://localhost:65080';

    const getTickerList = () => {
        fetch(`${urlPrefix}/ticker/list`).then((resp) => resp.json()).then(data => {
            setTickers(data);
        });
    };

    const handleChanges = (event) => {
        if (event.target.id === 'formTicker') {
            setTicker(event.target.value);
        } else if (event.target.id === 'formName') {
            setName(event.target.value);
        } else {
            console.log(event);
        }        
    }

    const saveTicker = () => {
        const payload = {
            'ticker': ticker,
            'name': name
        };

        console.log(payload);

        fetch(`${urlPrefix}/ticker`, { method: 'POST', mode: 'cors', cache: 'no-cache', body: JSON.stringify(payload)})
            .then(resp => {
                if (resp.status === 200) {
                    getTickerList();    
                }
            });
    }

    const addNewLine = () => {
        const newTickers = [...tickers];
        newTickers.push({'ticker': '', 'name': ''});

        setTickers(newTickers);

        document.getElementById('formTicker').value = '';
        document.getElementById('formName').value = '';
        document.getElementById('formTicker').focus();

    };

    const selectTicker = (idx) => {
        setTicker(tickers[idx]['ticker']);
        setName(tickers[idx]['name']);

        document.getElementById('formTicker').value = tickers[idx]['ticker'];
        document.getElementById('formName').value = tickers[idx]['name'];
        document.getElementById('formTicker').focus();
    }

    useEffect(() => {
        if (!loaded) {            
            setLoaded(true);
            getTickerList();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <Container>
            <Row>
                <Col>&nbsp;<br/>&nbsp;</Col>
            </Row>
            <Row>
                <Col><h2>Ticker Management</h2></Col>
                <Col style={{textAlign: 'right'}}><Button onClick={() => addNewLine()}>Add a ticker</Button></Col>
            </Row>
            <Row>
                <Col>
                    <Table striped bordered hover>
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Ticker</th>
                                <th>Name</th>
                            </tr>
                        </thead>
                        {
                            (!tickers || tickers.length === 0) ? null : (
                                <tbody>
                                    {
                                        tickers.map((tickerObj, idx) => (
                                            <tr key={'ticker_td_' + idx} onClick={() => selectTicker(idx)}>
                                                <td>{idx + 1}</td>
                                                <td>{tickerObj['ticker']}</td>
                                                <td>{tickerObj['name']}</td>
                                            </tr>
                                        ))
                                    }
                                </tbody>
                            )
                        }
                    </Table>
                </Col>
                <Col>
                    <Form>
                        <Form.Group className='mb-3' controlId='formTicker'>
                            <Form.Label>Ticker</Form.Label>
                            <Form.Control type='text' placeholder='Ticker' onChange={(event) => handleChanges(event)}/>
                        </Form.Group>
                        <Form.Group className='mb-3' controlId='formName'>
                            <Form.Label>Name</Form.Label>
                            <Form.Control type='text' placeholder='Name' onChange={(event) => handleChanges(event)}/>
                        </Form.Group>
                        <Button onClick={() => saveTicker()}>Save</Button>
                    </Form>
                </Col>
            </Row>

        </Container>
    );

}

export default Ticker;