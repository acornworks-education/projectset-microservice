import { useEffect, useState } from 'react';
import {Container, Row, Col, Table, ButtonGroup, ToggleButton} from 'react-bootstrap';
import { ComposedChart, Area, Line, XAxis, YAxis, CartesianGrid, Tooltip } from 'recharts';


function Strategy() {
    const [ tickers, setTickers ] = useState([]);
    const [ loaded, setLoaded ] = useState(false);
    const urlPrefix = !!process.env.REACT_APP_ENDPOINT ? process.env.REACT_APP_ENDPOINT : 'http://localhost:65080';
    const [ price, setPrice ] = useState(0);
    const [ chartWidth, setChartWidth ] = useState(500);
    const [ currentStrategy, setCurrentStrategy ] = useState('ADX');
    const [ currentTicker, setCurrentTicker ] = useState(null);
    const [ grossReturn, setGrossReturn ] = useState(0);
    const [ analysisResults, setAnalysisResults ] = useState([]);

    const strategies = [
        'ADX',
        'CCICorrelation',
        'GlobalExtrema',
        'MovingMomentum',
        'RSI2'
    ];

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

    useEffect(() => {
        if (!loaded) {
            setLoaded(true);
            getTickerList();
            setChartWidth(document.getElementById('tblStrategy').offsetWidth);            
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const selectTicker = (idx) => {
        const targetTicker = tickers[idx]['ticker'];
        getCurrentPrice(targetTicker);
        setCurrentTicker(targetTicker);
        getAnalysisResult(targetTicker, currentStrategy);
    }

    const setRadioValue = (selectedStrategy) => {
        setCurrentStrategy(selectedStrategy);

        if (!!currentTicker) {
            getAnalysisResult(currentTicker, selectedStrategy);
        }
    }

    const getAnalysisResult = (ticker, strategy) => {
        fetch(`${urlPrefix}/strategy/${ticker}/${strategy}`).then(resp => resp.json()).then(data => {
            setGrossReturn(data['grossReturn']);

            const convertedList = data['stockPrices'].map(trade => {
                trade['buy'] = !!trade['buy'] ? 1 : 0;
                trade['hold'] = !!trade['hold'] ? 1 : 0;

                return trade;
            })

            setAnalysisResults(convertedList);
        });
    }

    return (
        <>
        <Container>
            <Row>
                <Col>&nbsp;<br/>&nbsp;</Col>
            </Row>
            <Row>
                <Col><h2>Strategy Analyzer</h2></Col>
                <Col>
                    <ButtonGroup>
                        {strategies.map((radio, idx) => (
                            <ToggleButton
                                key={idx}
                                id={`radio-${idx}`}
                                type="radio"
                                variant={idx % 2 ? 'outline-success' : 'outline-danger'}
                                name="radio"
                                value={radio}
                                checked={currentStrategy === radio}
                                onChange={(e) => setRadioValue(e.currentTarget.value)}
                            >{radio}</ToggleButton>
                        ))}
                    </ButtonGroup>
                </Col>
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
                            <Col>Gross Return: {grossReturn}</Col>
                        </Row>
                        <Row>
                            <Col>&nbsp;</Col>
                        </Row>
                        <Row>
                            <Col style={{textAlign: 'center'}}>
                                <ComposedChart data={analysisResults} width={chartWidth} height={200} syncId='analysis'>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="date" />
                                    <YAxis />
                                    <Tooltip />
                                    <Line type="monotone" dataKey="close" stackId="1" stroke="#8884d8" fill="#8884d8" />
                                </ComposedChart>
                                <ComposedChart data={analysisResults} width={chartWidth} height={100} syncId='analysis'>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="date" />
                                    <YAxis />
                                    <Tooltip />                                    
                                    <Area type="monotone" dataKey="volume" stackId="1" stroke="#82ca9d" fill="#82ca9d" />
                                </ComposedChart>
                                <ComposedChart data={analysisResults} width={chartWidth} height={100} syncId='analysis'>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="date" />
                                    <YAxis />
                                    <Tooltip />                                    
                                    <Area type="monotone" dataKey="hold" stackId="1" stroke="#82ca9d" fill="#82ca9d" />
                                </ComposedChart>
                            </Col>
                        </Row>
                        <Row>
                            <Col>&nbsp;</Col>
                        </Row>
                        <Row>
                            <Col>
                                <Table striped bordered hover size='sm' id='tblStrategy'>
                                    <thead>
                                        <tr>
                                            <th>Date</th>
                                            <th>Close</th>
                                            <th>Volume</th>
                                            <th>Buy</th>
                                            <th>Hold</th>
                                        </tr>
                                    </thead>
                                    {
                                        (!analysisResults || analysisResults.length === 0) ? null : (
                                            <tbody>
                                            {
                                                analysisResults.map((result, idx) => (
                                                    <tr key={`analysis_tr_${idx}`}>
                                                        <td>{result['date']}</td>
                                                        <td>{result['close']}</td>
                                                        <td>{result['volume']}</td>
                                                        <td>{result['buy'] === 1 ? 'BUY': ''}</td>
                                                        <td>{result['hold'] === 1 ? 'HOLD' : '' }</td>
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

export default Strategy;
