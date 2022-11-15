import Navbar from 'react-bootstrap/Navbar';
import Nav from 'react-bootstrap/Nav';
import Container from 'react-bootstrap/Container';
import Ticker from './Ticker';
import Price from './Price';
import Strategy from './Strategy';

import { useState } from 'react';

function App() {
  const [currentTab, setCurrentTab] = useState('ticker');

  const getTargetTab = () => {
    if (currentTab === 'ticker') {
      return (<Ticker/>);
    } else if (currentTab === 'price') {
      return (<Price/>);
    } else if (currentTab === 'strategy') {
      return (<Strategy/>);
    }
  }

  return (
    <>      
      <Navbar bg='light' fixed='top'>
        <Container>
          <Navbar.Brand>AcornWorks Educations</Navbar.Brand>
          <Nav className='me-auto'>
            <Nav.Link onClick={() => setCurrentTab('ticker')}>Ticker Management</Nav.Link>
            <Nav.Link onClick={() => setCurrentTab('price')}>Price</Nav.Link>
            <Nav.Link onClick={() => setCurrentTab('strategy')}>Strategy</Nav.Link>
          </Nav>
        </Container>      
      </Navbar>
      <br/>
      {getTargetTab()}
    </>
  );
}

export default App;
