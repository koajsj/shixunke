/*
  汽车租赁系统数据库脚本
  数据库名：car_rental
  编码：utf8mb4
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 表结构
-- =========================
DROP TABLE IF EXISTS `Payment`;
DROP TABLE IF EXISTS `Rental`;
DROP TABLE IF EXISTS `Car`;
DROP TABLE IF EXISTS `Customer`;

CREATE TABLE `Customer` (
  `CustomerID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `Name` VARCHAR(50) NOT NULL COMMENT '客户姓名',
  `Phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `Email` VARCHAR(100) NOT NULL COMMENT '电子邮箱',
  `LicenseNumber` VARCHAR(30) NOT NULL COMMENT '驾驶证号',
  PRIMARY KEY (`CustomerID`),
  UNIQUE KEY `uk_customer_phone` (`Phone`),
  UNIQUE KEY `uk_customer_email` (`Email`),
  UNIQUE KEY `uk_customer_license_number` (`LicenseNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='客户表';

CREATE TABLE `Car` (
  `CarID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '汽车ID',
  `PlateNumber` VARCHAR(20) NOT NULL COMMENT '车牌号',
  `Brand` VARCHAR(50) NOT NULL COMMENT '品牌',
  `Model` VARCHAR(50) NOT NULL COMMENT '型号',
  `Color` VARCHAR(20) NOT NULL COMMENT '颜色',
  `Year` YEAR NOT NULL COMMENT '年份',
  `Status` ENUM('可租','已租','维修') NOT NULL DEFAULT '可租' COMMENT '车辆状态',
  `DailyRate` DECIMAL(10,2) NOT NULL COMMENT '日租金',
  PRIMARY KEY (`CarID`),
  UNIQUE KEY `uk_car_plate_number` (`PlateNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='汽车表';

CREATE TABLE `Rental` (
  `RentalID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '租赁记录ID',
  `CustomerID` BIGINT UNSIGNED NOT NULL COMMENT '客户ID',
  `CarID` BIGINT UNSIGNED NOT NULL COMMENT '汽车ID',
  `RentalDate` DATE NOT NULL COMMENT '租赁日期',
  `ReturnDate` DATE NOT NULL COMMENT '预计归还日期',
  `ActualReturnDate` DATE DEFAULT NULL COMMENT '实际归还日期',
  `TotalAmount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
  `Status` ENUM('待支付','租赁中','已完成','已取消') NOT NULL DEFAULT '待支付' COMMENT '租赁状态',
  PRIMARY KEY (`RentalID`),
  KEY `idx_rental_customer_id` (`CustomerID`),
  KEY `idx_rental_car_id` (`CarID`),
  CONSTRAINT `fk_rental_customer`
    FOREIGN KEY (`CustomerID`) REFERENCES `Customer` (`CustomerID`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT `fk_rental_car`
    FOREIGN KEY (`CarID`) REFERENCES `Car` (`CarID`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT `chk_rental_total_amount`
    CHECK (`TotalAmount` >= 0),
  CONSTRAINT `chk_rental_dates`
    CHECK (`ReturnDate` >= `RentalDate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='租赁记录表';

CREATE TABLE `Payment` (
  `PaymentID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '支付ID',
  `RentalID` BIGINT UNSIGNED NOT NULL COMMENT '租赁记录ID',
  `PaymentDate` DATE NOT NULL COMMENT '支付日期',
  `Amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `PaymentMethod` ENUM('微信','支付宝','银行卡','现金') NOT NULL COMMENT '支付方式',
  PRIMARY KEY (`PaymentID`),
  KEY `idx_payment_rental_id` (`RentalID`),
  CONSTRAINT `fk_payment_rental`
    FOREIGN KEY (`RentalID`) REFERENCES `Rental` (`RentalID`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT `chk_payment_amount`
    CHECK (`Amount` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付表';

-- =========================
-- 示例数据
-- =========================
INSERT INTO `Customer` (`CustomerID`, `Name`, `Phone`, `Email`, `LicenseNumber`) VALUES
(1, '张三', '13800000001', 'zhangsan@example.com', 'DL100001'),
(2, '李四', '13800000002', 'lisi@example.com', 'DL100002');

INSERT INTO `Car` (`CarID`, `PlateNumber`, `Brand`, `Model`, `Color`, `Year`, `Status`, `DailyRate`) VALUES
(1, '京A12345', '丰田', '凯美瑞', '白色', 2022, '已租', 260.00),
(2, '京B67890', '本田', '雅阁', '黑色', 2021, '可租', 240.00);

INSERT INTO `Rental` (`RentalID`, `CustomerID`, `CarID`, `RentalDate`, `ReturnDate`, `ActualReturnDate`, `TotalAmount`, `Status`) VALUES
(1, 1, 1, '2026-06-01', '2026-06-04', NULL, 780.00, '租赁中'),
(2, 2, 2, '2026-06-03', '2026-06-05', '2026-06-05', 480.00, '已完成');

INSERT INTO `Payment` (`PaymentID`, `RentalID`, `PaymentDate`, `Amount`, `PaymentMethod`) VALUES
(1, 1, '2026-06-01', 780.00, '微信'),
(2, 2, '2026-06-03', 480.00, '支付宝');

SET FOREIGN_KEY_CHECKS = 1;
