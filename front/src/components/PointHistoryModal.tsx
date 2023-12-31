import React, { ReactElement, useState } from 'react';
import { LanguageState } from "states/states";
import { accessTokenState, refreshTokenState} from "states/states";
import { useRecoilState } from "recoil";
import axios from "axios";
import { render } from 'react-dom';
import { useRouter } from "next/router";

interface props {
	open: boolean;
	close: (v: boolean) => void;
}

const Modal = (props: props): ReactElement => {
	const [lang, setLang] = useRecoilState(LanguageState);
	const [accessToken, setAccessToken] = useRecoilState(accessTokenState);
	const [refreshToken, setRefreshToken] = useRecoilState(refreshTokenState);
	const [history, setHistory] = useState<any[]>();

	const { open, close } = props;

	const itemsPerPage = 10;
	const [currentPage, setCurrentPage] = useState(1);

	const router = useRouter();

	// 포인트 적립 내역 조회
	axios.get("https://stepup-pi.com:8080/api/rank/my/history", {
		headers: {
			Authorization: `Bearer ${accessToken}`
		},
	}).then((data) => {
		if (data.data.message === "포인트 적립 내역 조회 완료") {
			setHistory(data.data.data);
		} else {
			{ lang === "en" ? alert("Failed to retrieve. Please try again.") : lang === "cn" ? alert("检索失败，请重试。") : alert("조회에 실패하였습니다. 다시 시도해주세요.") }
		}
	}).catch((error: any) => {
		if (error.response.data.message === "만료된 토큰") {
			axios.get("https://stepup-pi.com:8080/api/rank/my/history", {
				headers: {
					refreshToken: refreshToken
				},
			}).then((data) => {
				if (data.data.message === "토큰 재발급 완료") {
					setAccessToken(data.data.data.accessToken);
					setRefreshToken(data.data.data.refreshToken);
				}
			}).then(() => {
				axios.get("https://stepup-pi.com:8080/api/rank/my/history", {
					headers: {
						Authorization: `Bearer ${accessToken}`
					},
				}).then((data) => {
					if (data.data.message === "포인트 적립 내역 조회 완료") {
						setHistory(data.data.data);
					} else {
						{ lang === "en" ? alert("Failed to retrieve. Please try again.") : lang === "cn" ? alert("检索失败，请重试。") : alert("조회에 실패하였습니다. 다시 시도해주세요.") }
					}
				}).catch((data) => {
					if (data.response.status === 401) {
						alert("장시간 이용하지 않아 자동 로그아웃 되었습니다.");
						router.push("/login");
						return;
					}

					if (data.response.status === 500) {
						alert("시스템 에러, 관리자에게 문의하세요.");
						return;
					}
				})
			})
		}
	})

	// 페이지네이션
	const handlePageChange = (pageNumber: any) => {
		setCurrentPage(pageNumber);
	};
	const renderItems = () => {
		const startIndex = (currentPage - 1) * itemsPerPage;
		const endIndex = startIndex + itemsPerPage;

		return (
			<tbody>
				{history?.slice(startIndex, endIndex)?.map((item, index) => (
					<tr key={index}>
						<td className="point-history-content-category">{getLabel(item.pointType)}</td>
						<td className="point-history-content-point">{item.point * item.count}</td>
						<td className="point-history-content-title">{item.randomDanceTitle}</td>
					</tr>
				))}
			</tbody>
		);
	};

	const getLabel = (pointType: any) => {
		switch (pointType) {
			case "PRACTICE_ROOM":
				return "연습실 이용";
			case "FIRST_PRIZE":
				return "랜플댄 우승";
			case "SUCCESS_MUSIC":
				return "랜플댄 참가";
			case "OPEN_DANCE":
				return "랜플댄 개최";
			default:
				return "-";
		}
	};

	return (
		<div className="ce-modal">
			<div className="modal-wrap">
				<div className="modal-bg" onClick={close}></div>
				<div className="modal-container-point">
					<h2 className="point-history-modal-title">포인트 적립 내역</h2>
					<div>
						<table className="point-history-table">
							<thead>
								<tr className="point-history-item">
									<th className="point-history-category">분류</th>
									<th className="point-history-point">포인트</th>
									<th className="point-history-title">제목</th>
								</tr>
							</thead>
							{renderItems()}
						</table>
					</div>
					<div className="btn-pagination">
						{history && Array.from({ length: Math.ceil(history.length / itemsPerPage) }, (_, index) => (
							<button
								key={index}
								onClick={() => handlePageChange(index + 1)}
								className={currentPage === index + 1 ? "active" : ""}
							>
								{index + 1}
							</button>
						))}
					</div>
					<button onClick={close} className="check">{lang === "en" ? "CHECK" : lang === "cn" ? "确认" : "확인"}</button>
				</div>
			</div>
		</div>
	)
}

export default Modal;