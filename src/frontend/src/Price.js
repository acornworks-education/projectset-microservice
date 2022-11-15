import { useEffect, useState } from 'react';
import {Container, Row, Col, Table} from 'react-bootstrap';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip } from 'recharts';

function Price() {
    const [ tickers, setTickers ] = useState([]);
    const [ loaded, setLoaded ] = useState(false);
    const urlPrefix = !!process.env.REACT_APP_ENDPOINT ? process.env.REACT_APP_ENDPOINT : 'http://localhost:65080';
    const [ price, setPrice ] = useState(0);
    const [ historicalPrices, setHistoricalPrices ] = useState([]);
    const [ chartWidth, setChartWidth ] = useState(500);


    const getTickerList = () => {
        fetch(`${urlPrefix}/ticker/list`).then((resp) => resp.json()).then(data => {
            setTickers(data);
        });
    };

    const getCurrentPrice = (ticker) => {
        fetch(`${urlPrefix}/price/spot/${ticker}`).then(resp => resp.json()).then(data => {
            setPrice(data['price']);
        });
    };

    const getHistoricalPrice = (ticker) => {
        fetch(`${urlPrefix}/price/historical/${ticker}`).then(resp => resp.json()).then(data => {
            setHistoricalPrices(data);
        });
    }

    useEffect(() => {
        if (!loaded) {
            setLoaded(true);
            getTickerList();
            setChartWidth(document.getElementById('tblPrice').offsetWidth);            
        }

        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const selectTicker = (idx) => {
        const targetTicker = tickers[idx]['ticker'];
        getCurrentPrice(targetTicker);
        getHistoricalPrice(targetTicker);
    }

    return (
        <>
        <Container>
            <Row>
                <Col>&nbsp;<br/>&nbsp;</Col>
            </Row>
            <Row>
                <Col><h2>Price Checker</h2></Col>
            </Row>
            <Row>
                <Col md='3'>
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
                <Col md='9'>
                    <Container>
                        <Row>
                            <Col>Current Price: {price}</Col>
                        </Row>
                        <Row>
                            <Col>&nbsp;</Col>
                        </Row>
                        <Row>
                            <Col style={{textAlign: 'center'}}>
                                <AreaChart data={historicalPrices} width={chartWidth} height={300}>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="date" />
                                    <YAxis />
                                    <Tooltip />
                                    <Area type="monotone" dataKey="high" stackId="1" stroke="#8884d8" fill="#8884d8" />
                                    <Area type="monotone" dataKey="close" stackId="1" stroke="#82ca9d" fill="#82ca9d" />
                                    <Area type="monotone" dataKey="low" stackId="1" stroke="#ffc658" fill="#ffc658" />
                                </AreaChart>
                            </Col>
                        </Row>
                        <Row>
                            <Col>&nbsp;</Col>
                        </Row>
                        <Row>
                            <Col>
                                <Table striped bordered hover size='sm' id='tblPrice'>
                                    <thead>
                                        <tr>
                                            <th>Date</th>
                                            <th>Open</th>
                                            <th>High</th>
                                            <th>Low</th>
                                            <th>Close</th>
                                            <th>Volume</th>
                                        </tr>
                                    </thead>
                                    {
                                        (!historicalPrices || historicalPrices.length === 0) ? null : (
                                            <tbody>
                                                {
                                                    historicalPrices.map((priceObj, idx) => (
                                                        <tr key={'price_tr_' + idx}>
                                                            <td>{priceObj['date']}</td>
                                                            <td>{priceObj['open']}</td>
                                                            <td>{priceObj['high']}</td>
                                                            <td>{priceObj['low']}</td>
                                                            <td>{priceObj['close']}</td>
                                                            <td>{priceObj['volume']}</td>
                                                        </tr>
                                                    ))
                                                }
                                            </tbody>
                                        )
                                    }
                                </Table>

                            </Col>
                        </Row>
                    </Container>
                </Col>
            </Row>
        </Container>
        </>
    );
}

export default Price;
