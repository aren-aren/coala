<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<!DOCTYPE html>
<html lang="en">

<c:import url="../temps/header_css.jsp"></c:import>
<link rel="stylesheet" href="/resources/css/quiz/quiz.css">

<body>

<!-- ***** Preloader Start ***** -->
<div id="js-preloader" class="js-preloader">
    <div class="preloader-inner">
        <span class="dot"></span>
        <div class="dots">
            <span></span>
            <span></span>
            <span></span>
        </div>
    </div>
</div>
<!-- ***** Preloader End ***** -->

<!-- ***** Header Area Start ***** -->
<c:import url="../temps/header.jsp"></c:import>
<!-- ***** Header Area End ***** -->

<div class="container">
    <div class="row">
        <div class="col-lg-12">
            <div class="page-content">

                <!-- ***** Featured Games Start ***** -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="gaming-library">
                            <div class="heading-section">
                                <div class="text-white text-center">
                                    LV.${dto.quiz_Level}
                                </div>
                                <h4 class="middle-title mb-0"><a href="solve?quiz_No=${dto.quiz_No}">#${dto.quiz_No}</a>
                                </h4>
                                <h4 class="middle-title mb-0">${dto.quiz_Title}</h4>
                            </div>
                        </div>
                        <div>
                            <div class="col-12">

                                <div class="my-3">
                                    <ul>
                                        <li>
                                            <div class="card text-bg-dark mb-3">
                                                <div class="card-body">
                                                    <div class="mb-2">
                                                        <img src="${avatar}">
                                                        <h5 class="card-title d-inline-block">${list[0].memberDTO.member_Nick}</h5>
                                                        <div class="float-end">${list[0].answer_Date}</div>
                                                    </div>
                                                    <wc-codemirror mode="text/x-java"
                                                                   theme="tomorrow-night-eighties"
                                                                   readonly="nocursor">
<script type="wc-content">
${list[0].sourcecode}
</script>
                                                    </wc-codemirror>
                                                    <a href="#" class="card-link">Card link</a>
                                                    <a href="#" class="card-link">Another link</a>
                                                </div>
                                            </div>
                                        </li>
                                    </ul>
                                </div>

                                <div id="paging-div" data-page="${pager.page}">
                                    <nav aria-label="Page navigation example">
                                        <ul class="pagination justify-content-center">
                                            <c:if test="${!pager.start}">
                                                <li class="page-item">
                                                    <button class="page-link" data-page="${pager.startNum-1}">Previous
                                                    </button>
                                                </li>
                                            </c:if>
                                            <c:forEach begin="${pager.startNum}" end="${pager.lastNum}" var="i">
                                                <li class="page-item">
                                                    <button class='page-link ${(pager.page == i)?"current-page":""}'
                                                            data-page="${i}">${i}</button>
                                                </li>
                                            </c:forEach>
                                            <c:if test="${!pager.last}">
                                                <li class="page-item">
                                                    <button class="page-link" data-page="${pager.lastNum+1}">Next
                                                    </button>
                                                </li>
                                            </c:if>
                                        </ul>
                                    </nav>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<c:import url="../temps/footer.jsp"></c:import>
<script type="module" src="https://cdn.jsdelivr.net/gh/vanillawc/wc-codemirror@1/index.js"></script>
<script type="module" src="https://cdn.jsdelivr.net/gh/vanillawc/wc-codemirror@1/mode/clike/clike.js"></script>
<link rel="stylesheet"
      href="https://cdn.jsdelivr.net/gh/vanillawc/wc-codemirror@1/theme/tomorrow-night-eighties.css">
<script src="/resources/js/quiz/list.js"></script>

</body>

</html>