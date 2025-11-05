import React, {useState} from "react";
import {
	Button,
	Card,
	Col,
	Container,
	Form,
	Row,
	Alert,
	Spinner,
} from "react-bootstrap";

function App() {
	interface PaymentResponse {
		id: number | string;
		partnerId: string;
		appliedFeeRate: number;
		feeAmount: number;
		netAmount: number;
		amount: number;
		cardLast4: string;
		approvalCode: string;
		approvedAt: string;
		status: string;
		createdAt: string;
	}
	interface TestPgExceptionResponse {
		code: string;
		errorCode: string;
		message: string;
		referenceId: number;
	}
	const [cardNumber, setCardNumber] = useState("1111-1111-1111-1111");
	const [birthDate, setBirthDate] = useState("19900101");
	const [expiry, setExpiry] = useState("1227");
	const [password, setPassword] = useState("12");
	const [amount, setAmount] = useState("10000");
	const [partnerId, setPartnerId] = useState("2");
	const [productName, setProductName] = useState("소고기");

	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState<TestPgExceptionResponse | null>(null);

	const [paymentResponse, setPaymentResponse] =
		useState<PaymentResponse | null>(null);

	const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
		event.preventDefault();
		setError(null);
		setIsLoading(true);

		if (!cardNumber || !birthDate || !expiry || !password || !partnerId) {
			setError(null);
			setIsLoading(false);
			return;
		}

		try {
			// --- 단계 2: 발급받은 토큰을 우리 백엔드로 전송하여 결제 요청 ---
			const requestToBackend = {
				cardNumber,
				birthDate,
				expiry,
				password,
				partnerId,
				amount,
				productName,
			}; // 다른 결제 정보와 함께 전송

			// 실제로는 fetch('/api/payment', { method: 'POST', body: requestJson, ... }) 호출
			const res = await fetch(
				"http://localhost:8080/api/v1/payments/test",
				{
					method: "POST",
					headers: {
						"Content-Type": "application/json",
					},
					body: JSON.stringify(requestToBackend),
				}
			);
			if (!res.ok) {
				const errorBody = await res.json();
				setError(errorBody);
				setIsLoading(false);
				return;
			}
			const result = await res.json();
			setPaymentResponse(result);
			setIsLoading(false);
		} catch (e) {}
	};

	return (
		<Container className="py-5">
			<Row className="justify-content-center">
				<Col md={6}>
					<Card>
						<Card.Header as="h4" className="text-center">
							카드 결제 (SDK 사용)
						</Card.Header>
						<Card.Body>
							<Alert variant="primary">
								ℹ️ 이 폼은 PG사 SDK가 제공하는 보안 입력창을
								시뮬레이션합니다.
							</Alert>
							<Form onSubmit={handleSubmit}>
								<Row>
									<Col>
										<Form.Group
											className="mb-3"
											controlId="cardNumber"
										>
											<Form.Label>카드 번호</Form.Label>
											<Form.Control
												type="text"
												placeholder="1111-1111-1111-1111"
												value={cardNumber}
												onChange={(e) =>
													setCardNumber(
														e.target.value
													)
												}
												disabled={isLoading}
											/>
										</Form.Group>
									</Col>
									<Col>
										<Form.Group
											className="mb-3"
											controlId="cardNumber"
										>
											<Form.Label>상품 이름</Form.Label>
											<Form.Control
												type="text"
												placeholder="김밥"
												value={productName}
												onChange={(e) =>
													setProductName(
														e.target.value
													)
												}
												disabled={isLoading}
											/>
										</Form.Group>
									</Col>
								</Row>
								<Row>
									<Col>
										<Form.Group
											className="mb-3"
											controlId="expiryDate"
										>
											<Form.Label>
												유효 기간 (MMYY)
											</Form.Label>
											<Form.Control
												type="text"
												placeholder="1227"
												value={expiry}
												onChange={(e) =>
													setExpiry(e.target.value)
												}
												disabled={isLoading}
											/>
										</Form.Group>
									</Col>
									<Col>
										<Form.Group
											className="mb-3"
											controlId="cvc"
										>
											<Form.Label>
												비밀번호 앞 두자리
											</Form.Label>
											<Form.Control
												type="text"
												placeholder="12"
												value={password}
												onChange={(e) =>
													setPassword(e.target.value)
												}
												disabled={isLoading}
											/>
										</Form.Group>
									</Col>
								</Row>

								<Row>
									<Col>
										<Form.Group
											className="mb-3"
											controlId="expiryDate"
										>
											<Form.Label>BirthDate</Form.Label>
											<Form.Control
												type="text"
												placeholder="19900101"
												value={birthDate}
												onChange={(e) =>
													setBirthDate(e.target.value)
												}
												disabled={isLoading}
											/>
										</Form.Group>
									</Col>
									<Col>
										<Form.Group
											className="mb-3"
											controlId="cvc"
										>
											<Form.Label>결제금액</Form.Label>
											<Form.Control
												type="text"
												placeholder="10000"
												value={amount}
												onChange={(e) =>
													setAmount(e.target.value)
												}
												disabled={isLoading}
											/>
										</Form.Group>
									</Col>
								</Row>
								<div className="d-grid">
									<Button
										variant="primary"
										type="submit"
										disabled={isLoading}
									>
										{isLoading ? (
											<Spinner
												as="span"
												animation="border"
												size="sm"
												role="status"
												aria-hidden="true"
											/>
										) : (
											`${amount}원 결제하기`
										)}
									</Button>
								</div>
							</Form>
						</Card.Body>
					</Card>

					{paymentResponse && (
						<div
							style={{
								marginTop: "20px",
								border: "1px solid #eee",
								padding: "15px",
							}}
						>
							<h2>✅ 결제 성공</h2>
							<ul style={{listStyleType: "none", padding: 0}}>
								<li>
									<strong>ID:</strong> {paymentResponse.id}
								</li>
								<li>
									<strong>파트너 ID:</strong>{" "}
									{paymentResponse.partnerId}
								</li>
								<li>
									<strong>결제 금액:</strong>{" "}
									{paymentResponse.amount} 원
								</li>
								<li>
									<strong>수수료율:</strong>{" "}
									{paymentResponse.appliedFeeRate}
								</li>
								<li>
									<strong>수수료:</strong>{" "}
									{paymentResponse.feeAmount} 원
								</li>
								<li>
									<strong>정산 금액:</strong>{" "}
									{paymentResponse.netAmount} 원
								</li>
								<li>
									<strong>카드 번호 (끝 4자리):</strong>{" "}
									{paymentResponse.cardLast4}
								</li>
								<li>
									<strong>승인 코드:</strong>{" "}
									{paymentResponse.approvalCode}
								</li>
								<li>
									<strong>승인 시각:</strong>{" "}
									{new Date(
										paymentResponse.approvedAt
									).toDateString()}
								</li>
								<li>
									<strong>상태:</strong>{" "}
									{paymentResponse.status}
								</li>
								<li>
									<strong>생성 시각:</strong>{" "}
									{new Date(
										paymentResponse.createdAt
									).toDateString()}
								</li>{" "}
							</ul>
							<hr />
							<h3>전체 응답 데이터 (디버깅용)</h3>
							<pre
								style={{
									backgroundColor: "#f4f4f4",
									padding: "10px",
								}}
							>
								{JSON.stringify(paymentResponse, null, 2)}
							</pre>
						</div>
					)}
					{error && (
						<div>
							<h4>오류</h4>
							<p>{error.code}</p>
							<p>{error.errorCode}</p>
							<p>{error.message}</p>
							<p>{error.referenceId}</p>
						</div>
					)}
				</Col>
			</Row>
		</Container>
	);
}

export default App;
