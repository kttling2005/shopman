<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>

        <!doctype html>
        <html lang="vi">

        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Trang chủ · ShopMan</title>

            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"
                rel="stylesheet">

            <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
        </head>

        <body class="">

            <!-- TOP STRIP -->
            <div class="strip">
                <span>ShopMan · Hệ thống quản lý bán hàng bách hóa trực tuyến</span>
                <span>Hotline 1900 0000 · Giao hàng nội thành trong ngày</span>
            </div>

            <!-- HEADER -->
            <header class="hdr">

                <a href="${pageContext.request.contextPath}/">
                    <div class="brand">
                        <div class="mark">S</div>
                        ShopMan
                    </div>
                </a>

                <div class="search">
                    <input class="input" placeholder="Tìm hàng hóa, mã hàng, từ khóa…">
                </div>

                <div class="right-nav">

                    <!-- VISITOR: Chưa đăng nhập -->
                    <c:if test="${empty sessionScope.memberId}">

                        <a href="${pageContext.request.contextPath}/register" class="btn primary">
                            Đăng ký thành viên
                        </a>

                        <a href="${pageContext.request.contextPath}/login" class="btn">
                            Đăng nhập
                        </a>

                    </c:if>

                    <!-- MEMBER / CUSTOMER: Đã đăng nhập -->
                    <c:if test="${not empty sessionScope.memberId}">

                        <a href="${pageContext.request.contextPath}/cart" class="cart-pill">
                            Giỏ hàng
                        </a>

                        <a href="${pageContext.request.contextPath}/profile">
                            <c:out value="${sessionScope.displayName}" />
                        </a>

                        <a href="${pageContext.request.contextPath}/logout" class="muted">
                            Đăng xuất
                        </a>

                    </c:if>

                </div>
            </header>

            <!-- SUB NAVIGATION -->
            <div class="nav-sub">

                <a class="active" href="${pageContext.request.contextPath}/">
                    Trang chủ
                </a>

                <a href="${pageContext.request.contextPath}/products">
                    Khám phá
                </a>

                <a href="${pageContext.request.contextPath}/cart">
                    Giỏ hàng
                </a>

                <!-- Chỉ Member mới có trang đơn hàng -->
                <c:if test="${not empty sessionScope.memberId}">
                    <a href="${pageContext.request.contextPath}/my-orders">
                        Đơn hàng của tôi
                    </a>

                    <a href="${pageContext.request.contextPath}/profile">
                        Tài khoản
                    </a>
                </c:if>

            </div>

            <!-- MAIN CONTENT -->
            <main class="container">

                <!-- GREETING -->
                <c:choose>
                    <c:when test="${empty sessionScope.memberId}">
                        <p class="small muted">
                            Chào mừng bạn đến với ShopMan!
                        </p>

                        <h1 style="margin:4px 0 4px">
                            Hôm nay bạn cần gì?
                        </h1>

                        <p class="muted" style="margin-bottom:18px">
                            Khám phá hàng hóa và lựa chọn sản phẩm phù hợp
                            với nhu cầu của bạn.
                        </p>
                    </c:when>

                    <c:otherwise>
                        <p class="small muted">
                            Xin chào,
                            <c:out value="${sessionScope.displayName}" />
                        </p>

                        <h1 style="margin:4px 0 4px">
                            Hôm nay bạn cần gì?
                        </h1>

                        <p class="muted" style="margin-bottom:18px">
                            Tìm kiếm, đặt hàng và theo dõi đơn trong một nơi.
                        </p>
                    </c:otherwise>
                </c:choose>


                <!-- HERO SECTION -->
                <div class="hero">

                    <div class="copy">

                        <span class="tag">Ưu đãi tuần này</span>

                        <h1 style="font-size:30px">
                            Chọn đồ tốt cho<br>
                            những ngày bình thường.
                        </h1>

                        <p class="muted">
                            Miễn phí giao hàng cho đơn từ 300.000 đ.
                        </p>

                        <div>
                            <a class="btn primary lg" href="/products.html">
                                Khám phá hàng hóa
                            </a>
                        </div>

                    </div>

                    <div class="ph warm" style="height:240px">
                        <span>Ảnh bếp</span>
                    </div>

                </div>


                <!-- PRODUCT CATEGORIES -->
                <div class="cat-row mt24">

                    <a class="cat-chip" href="products.html">
                        <i>🍚</i> Thực phẩm
                    </a>

                    <a class="cat-chip" href="/products.html">
                        <i>🥤</i> Đồ uống
                    </a>

                    <a class="cat-chip" href="/products.html">
                        <i>🧴</i> Hóa mỹ phẩm
                    </a>

                    <a class="cat-chip" href="/products.html">
                        <i>🍳</i> Đồ gia dụng
                    </a>

                    <a class="cat-chip" href="/products.html">
                        <i>🥛</i> Sữa & bánh
                    </a>

                    <a class="cat-chip" href="/products.html">
                        <i>🥬</i> Rau củ quả
                    </a>

                </div>


                <!-- FEATURED PRODUCTS HEADER -->
                <div class="row between mt24" style="margin-bottom:12px">

                    <h2>Được chọn cho bạn</h2>

                    <a class="link" href="/products.html">
                        Xem tất cả →
                    </a>

                </div>


                <!-- FEATURED PRODUCTS -->
                <div class="grid g4">

                    <!-- PRODUCT 1 -->
                    <div class="pcard">

                        <a href="/products.html">
                            <div class="ph oven">
                                <span>Ảnh sản phẩm</span>
                            </div>
                        </a>

                        <div class="cat">Đồ gia dụng</div>

                        <a href="/products.html" class="name">
                            Lò nướng ABC 30L
                        </a>

                        <div class="row between">

                            <span class="price">
                                4.375.000 đ
                            </span>

                            <a class="btn sm primary" href="/cart.html">
                                Thêm
                            </a>

                        </div>

                    </div>


                    <!-- PRODUCT 2 -->
                    <div class="pcard">

                        <a href="/products.html">
                            <div class="ph warm">
                                <span>Ảnh sản phẩm</span>
                            </div>
                        </a>

                        <div class="cat">Thực phẩm</div>

                        <a href="/products.html" class="name">
                            Bánh quy XYZ
                        </a>

                        <div class="row between">

                            <span class="price">
                                20.000 đ
                            </span>

                            <a class="btn sm primary" href="/cart.html">
                                Thêm
                            </a>

                        </div>

                    </div>


                    <!-- PRODUCT 3 -->
                    <div class="pcard">

                        <a href="/products.html">
                            <div class="ph green">
                                <span>Ảnh sản phẩm</span>
                            </div>
                        </a>

                        <div class="cat">Thực phẩm</div>

                        <a href="/products.html" class="name">
                            Dầu mè nguyên chất
                        </a>

                        <div class="row between">

                            <span class="price">
                                65.000 đ
                            </span>

                            <a class="btn sm primary" href="/cart.html">
                                Thêm
                            </a>

                        </div>

                    </div>


                    <!-- PRODUCT 4 -->
                    <div class="pcard">

                        <a href="/products.html">
                            <div class="ph green">
                                <span>Ảnh sản phẩm</span>
                            </div>
                        </a>

                        <div class="cat">Hóa mỹ phẩm</div>

                        <a href="/products.html" class="name">
                            Bột giặt OMO 3kg
                        </a>

                        <div class="row between">

                            <span class="price">
                                145.000 đ
                            </span>

                            <a class="btn sm primary" href="/cart.html">
                                Thêm
                            </a>

                        </div>

                    </div>

                </div>


                <!-- SHOP BENEFITS -->
                <div class="grid g3 mt24">

                    <section class="card">
                        <b>Giao nhanh nội thành</b>

                        <p class="muted small mt8">
                            Nhận hàng trong ngày với đơn đặt trước 16:00.
                        </p>
                    </section>

                    <section class="card">
                        <b>Hàng chính hãng</b>

                        <p class="muted small mt8">
                            Nhập trực tiếp từ nhà cung cấp uy tín.
                        </p>
                    </section>

                    <section class="card">
                        <b>Theo dõi đơn rõ ràng</b>

                        <p class="muted small mt8">
                            Cập nhật trạng thái từng bước xử lý.
                        </p>
                    </section>

                </div>

            </main>

        </body>

        </html>